package com.taogen.datahandling.es.service;

import com.taogen.datahandling.es.vo.DslQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class EsReaderTest {

    @Autowired
    private RestClient restClient;

    @Autowired
    private EsReader esReader;

    @Test
    @Disabled
    void count() {
        DslQueryParam dslQueryParam = new DslQueryParam();
        dslQueryParam.setIndex(Arrays.asList("alias-meta-20230901"));
        dslQueryParam.setDsl("{\n" +
                "    \"query\": {\n" +
                "        \"bool\": {\n" +
                "            \"must\": {\n" +
                "              \"range\": {\"pub_time\": {\"gte\": \"2023-09-01 00:10:00\",\"lte\": \"2023-09-01 00:11:00\"}}\n" +
                "          }\n" +
                "        }\n" +
                "    }\n" +
                "}");
        long count = esReader.count(restClient, dslQueryParam);
        log.debug("count: {}", count);
    }
    @Test
    @Disabled
    void readAll() throws IOException {
        DslQueryParam dslQueryParam = new DslQueryParam();
        dslQueryParam.setIndex(Arrays.asList("alias-meta-20230901"));
        dslQueryParam.setDsl("{\n" +
                "    \"size\": 50,\n" +
                "    \"_source\": [\"id\", \"pub_time\"],\n" +
                "    \"sort\": [\n" +
                "        {\n" +
                "            \"pub_time\": {\n" +
                "                \"order\": \"desc\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"query\": {\n" +
                "        \"bool\": {\n" +
                "            \"must\": {\n" +
                "              \"range\": {\"pub_time\": {\"gte\": \"2023-09-01 00:10:00\",\"lte\": \"2023-09-01 00:11:00\"}}\n" +
                "          }\n" +
                "        }\n" +
                "    }\n" +
                "}");
        List<JSONObject> labelAndData = esReader.readAll(restClient, dslQueryParam);
        log.debug("labelAndData: {}", labelAndData);
    }
}
