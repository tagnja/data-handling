package com.taogen.datahandling.es.util;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author taogen
 */
@Slf4j
public class HighLevelRestClientUtils {

    /**
     * Scroll query
     *
     * @param restHighLevelClient
     * @param index
     * @param builder
     * @return JSON string list
     * @throws IOException
     */
    public static List<String> scrollQuery(RestHighLevelClient restHighLevelClient,
                                           String[] index,
                                           SearchSourceBuilder builder) throws IOException {
        log.debug("index name is {}", Arrays.toString(index));
        log.debug("searchSourceBuilder = {}", builder.toString());
        long startTime = System.currentTimeMillis();
        List<String> resultList = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(index);
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        searchRequest.scroll(scroll);
        searchRequest.source(builder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        String scrollId = searchResponse.getScrollId();
        log.debug("scrollId = {}", scrollId);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        log.debug("hits size: {}", searchHits.length);
        Arrays.stream(searchHits).map(SearchHit::getSourceAsString).forEach(resultList::add);
        while (searchHits != null && searchHits.length > 0) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = restHighLevelClient.searchScroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            log.debug("scrollId = {}", scrollId);
            searchHits = searchResponse.getHits().getHits();
            log.debug("hits size: {}", searchHits.length);
            Arrays.stream(searchHits).map(SearchHit::getSourceAsString).forEach(resultList::add);
        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        Boolean succeeded = clearScrollResponse.isSucceeded();
        log.debug("succeeded = {}", succeeded);
        log.debug("query es cost time = {}", (System.currentTimeMillis() - startTime));
        return resultList;
    }

}
