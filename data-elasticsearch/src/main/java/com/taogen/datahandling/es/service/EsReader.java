package com.taogen.datahandling.es.service;

import com.taogen.datahandling.es.vo.DslQueryParam;
import org.elasticsearch.client.RestClient;
import org.json.JSONObject;
import org.springframework.data.redis.connection.RedisConnection;

import java.io.IOException;
import java.util.List;

/**
 * @author taogen
 */
public interface EsReader {

    /**
     * Read all data from elasticsearch
     * <p>
     * size in dsl is scroll fetch size. If size is null, it will use default size 10.
     * It's better to specify what fields you want to get in _source.
     *
     * @param restClient
     * @param dslQueryParam
     * @return
     * @throws IOException
     */
    List<JSONObject> readAll(RestClient restClient, DslQueryParam dslQueryParam) throws IOException;

    List<JSONObject> readAllBatchWithCache(RestClient restClient,
                                           DslQueryParam dslQueryParam,
                                           RedisConnection redisConnection) throws IOException;

    long count(RestClient restClient, DslQueryParam dslQueryParam);
}
