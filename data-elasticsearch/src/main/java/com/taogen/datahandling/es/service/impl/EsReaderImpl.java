package com.taogen.datahandling.es.service.impl;

import com.taogen.commons.collection.CollectionUtils;
import com.taogen.commons.crypto.HashUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.es.service.EsReader;
import com.taogen.datahandling.es.util.LowLevelRestClientUtils;
import com.taogen.datahandling.es.vo.DslQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.json.JSONObject;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author taogen
 */
@Component
@Slf4j
public class EsReaderImpl implements EsReader {
    @Override
    public List<JSONObject> readAll(RestClient restClient,
                                    DslQueryParam dslQueryParam,
                                    Function<List<JSONObject>, List<JSONObject>> dataConverter) throws IOException {
        long startTime = System.currentTimeMillis();
        List<JSONObject> itemJsonList = new ArrayList<>();
        if (dslQueryParam.isBatch()) {
            for (String index : dslQueryParam.getIndex()) {
                List<JSONObject> tempItemJsonList = LowLevelRestClientUtils.scrollQuery(restClient, Arrays.asList(index), dslQueryParam.getDsl());
                if (CollectionUtils.isNotEmpty(tempItemJsonList)) {
                    itemJsonList.addAll(tempItemJsonList);
                }
            }
        } else {
            itemJsonList.addAll(LowLevelRestClientUtils.scrollQuery(restClient, dslQueryParam.getIndex(), dslQueryParam.getDsl()));
        }
        if (dataConverter != null) {
            itemJsonList = dataConverter.apply(itemJsonList);
        }
        log.debug("Summary:\n====================\ntotal size: {}\nreadAll cost: {} ms\n====================",
                itemJsonList.size(), System.currentTimeMillis() - startTime);
        return itemJsonList;
    }

    /**
     * Read all data from elasticsearch with cache
     *
     * @param restClient
     * @param dslQueryParam   ignore batch field of dslQueryParam
     * @param redisConnection
     * @return
     * @throws IOException
     */
    @Override
    public List<JSONObject> readAllBatchWithCache(RestClient restClient,
                                                  DslQueryParam dslQueryParam,
                                                  RedisConnection redisConnection,
                                                  Function<List<JSONObject>, List<JSONObject>> dataConverter) throws IOException, ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();
        final String redisKeyTemplate = "es_data:index:%s:dsl:%s";
        List<JSONObject> itemJsonList = getJsonListBatchWithCache(restClient, dslQueryParam, redisConnection, redisKeyTemplate);
        if (dataConverter != null) {
            itemJsonList = dataConverter.apply(itemJsonList);
        }
        redisConnection.del(dslQueryParam.getIndex().stream()
                .map(item -> String.format(redisKeyTemplate, item, HashUtils.md5(dslQueryParam.getDsl())).getBytes()).collect(Collectors.toList()).toArray(new byte[][]{}));
        log.debug("delete redis keys when readAll successfully");
        log.debug("Summary:\n====================\ntotal size: {}\nreadAllWithCache cost: {} ms\n====================",
                itemJsonList.size(), System.currentTimeMillis() - startTime);
        return itemJsonList;
    }

    private List<JSONObject> getJsonListBatchWithCache(RestClient restClient,
                                                       DslQueryParam dslQueryParam,
                                                       RedisConnection redisConnection,
                                                       String redisKeyTemplate) throws IOException, InterruptedException, ExecutionException {
        // fetch and write data to redis if not exists
        if (dslQueryParam.isConcurrent()) {
            int threadNum = getConcurrentThreadNum(dslQueryParam.getThreadNum());
            log.debug("threadNum: {}", threadNum);
            ExecutorService executorService = null;
            try {
                executorService = Executors.newFixedThreadPool(threadNum);
                ExecutorCompletionService<String> completionService =
                        new ExecutorCompletionService<>(executorService);
                for (String index : dslQueryParam.getIndex()) {
                    completionService.submit(() -> {
                        String threadName = Thread.currentThread().getName();
                        log.debug("thread {} start to write data to redis", threadName);
                        writeDataToRedis(restClient, dslQueryParam, redisConnection, redisKeyTemplate, index);
                        return threadName;
                    });
                }
                for (int i = 0; i < dslQueryParam.getIndex().size(); i++) {
                    completionService.take().get();
                }
            } catch (Exception e) {
                if (executorService != null) {
                    executorService.shutdown();
                }
                log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
                throw e;
            }
        } else {
            for (String index : dslQueryParam.getIndex()) {
                writeDataToRedis(restClient, dslQueryParam, redisConnection, redisKeyTemplate, index);
            }
        }
        // read from redis
        List<JSONObject> resultJsonList = new ArrayList<>();
        for (String index : dslQueryParam.getIndex()) {
            String redisKey = String.format(redisKeyTemplate, index, HashUtils.md5(dslQueryParam.getDsl()));
            if (redisKeyExists(redisConnection, redisKey)) {
                List<byte[]> list = redisConnection.listCommands().lRange(redisKey.getBytes(), 0, -1);
                log.debug("read {} data from redis, size is {}", index, list.size());
                if (CollectionUtils.isNotEmpty(list)) {
                    List<JSONObject> tempJsonList = list.stream()
                            .map(item -> new JSONObject(new String(item)))
                            .collect(Collectors.toList());
                    resultJsonList.addAll(tempJsonList);
                }
            }
        }
        return resultJsonList;
    }

    private int getConcurrentThreadNum(Integer threadNum) {
        int cores = Runtime.getRuntime().availableProcessors();
        int maxThreadNum = cores * 2 + 1;
        if (threadNum != null && threadNum > 0 && threadNum <= maxThreadNum) {
            return threadNum;
        } else {
            return maxThreadNum;
        }
    }

    private void writeDataToRedis(RestClient restClient, DslQueryParam dslQueryParam, RedisConnection redisConnection, String redisKeyTemplate, String index) throws IOException {
        String redisKey = String.format(redisKeyTemplate, index, HashUtils.md5(dslQueryParam.getDsl()));
        if (!redisKeyExists(redisConnection, redisKey)) {
            List<JSONObject> tempJsonList = LowLevelRestClientUtils.scrollQuery(restClient, Arrays.asList(index), dslQueryParam.getDsl());
            if (!CollectionUtils.isEmpty(tempJsonList)) {
                redisConnection.listCommands().rPush(redisKey.getBytes(),
                        tempJsonList.stream().map(item -> item.toString().getBytes()).collect(Collectors.toList()).toArray(new byte[][]{}));
                log.debug("save {} data to redis, size is {}", index, tempJsonList.size());
                long longFor24Hours = 86400;
                redisConnection.expire(redisKey.getBytes(), longFor24Hours);
            }
        } else {
            log.debug("redis key exists - {}", redisKey);
        }
    }

    private boolean redisKeyExists(RedisConnection redisConnection, String redisKey) {
        Boolean keyExists = redisConnection.exists(redisKey.getBytes());
        return keyExists != null && keyExists;
    }

    @Override
    public long count(RestClient restClient, DslQueryParam dslQueryParam) {
        long startTime = System.currentTimeMillis();
        long count = 0;
        if (dslQueryParam.isBatch()) {
            for (String index : dslQueryParam.getIndex()) {
                count += LowLevelRestClientUtils.count(restClient, Arrays.asList(index), dslQueryParam.getDsl());
            }
        } else {
            count = LowLevelRestClientUtils.count(restClient, dslQueryParam.getIndex(), dslQueryParam.getDsl());
        }
        log.debug("Summary:\n====================\ntotal count: {}\ncount cost: {} ms\n====================",
                count, System.currentTimeMillis() - startTime);
        return count;
    }

    private LabelAndData convertToLabelAndData(List<JSONObject> itemJsonList, DslQueryParam dslQueryParam) {
        LabelAndData labelAndData = new LabelAndData();
        labelAndData.setLabels(dslQueryParam.getLabels());
        List<List<Object>> valuesList = new ArrayList<>();
        itemJsonList.forEach(jsonObject -> {
            Iterator<String> keys = jsonObject.keys();
            List<Object> values = new ArrayList<>();
            while (keys.hasNext()) {
                String key = keys.next();
                values.add(jsonObject.get(key));
            }
            valuesList.add(values);
        });
        labelAndData.setValuesList(valuesList);
        return labelAndData;
    }
}
