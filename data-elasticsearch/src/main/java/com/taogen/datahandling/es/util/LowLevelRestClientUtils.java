package com.taogen.datahandling.es.util;

import com.taogen.commons.jsonparser.orgjson.OrgJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author taogen
 */
@Slf4j
public class LowLevelRestClientUtils {
    /**
     * Scroll query
     *
     * @param restClient
     * @param index
     * @param dsl
     * @return JSON string list
     */
    public static List<JSONObject> scrollQuery(RestClient restClient,
                                               List<String> index,
                                               String dsl) throws IOException {
        List<JSONObject> result = new ArrayList<>();
        int searchTimes = 0;
        String endpoint = "/" + String.join(",", index) + "/_search?scroll=1m";
        JSONObject esResult = search(restClient, endpoint, dsl);
        int total = esResult.getJSONObject("hits").getInt("total");
        searchTimes++;
        String scrollId = esResult.getString("_scroll_id");
        log.debug("scrollId: {}", scrollId);
        JSONArray hits = esResult.getJSONObject("hits").getJSONArray("hits");
        while (hits != null && hits.length() > 0) {
            addHitsToList(result, hits);
            esResult = scrollSearch(restClient, scrollId);
            log.debug("scroll {} - {}/{}, {}", searchTimes, result.size(), total, result.size() * 100 / total + "%");
            searchTimes++;
            scrollId = esResult.getString("_scroll_id");
            hits = esResult.getJSONObject("hits").getJSONArray("hits");
        }
        log.debug("search times: {}", searchTimes);
        log.debug("result size: {}", result.size());
        return result;
    }

    private static void addHitsToList(List<JSONObject> result, JSONArray hits) {
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);
            JSONObject source = hit.getJSONObject("_source");
            result.add(source);
        }
    }

    private static JSONObject scrollSearch(RestClient restClient, String scrollId)
            throws IOException {
        String endpoint = "/_search/scroll";
        Request request = new Request("POST", endpoint);
        request.setJsonEntity("{\"scroll_id\":\"" + scrollId + "\", \"scroll\":\"1m\"}");
        Response response = restClient.performRequest(request);
        return getResponseJsonObj(response);
    }

    private static JSONObject search(RestClient restClient, String endpoint, String dsl) throws IOException {
        Request request = new Request("GET", endpoint);
        log.debug("endpoint: {}", endpoint);
        request.setJsonEntity(dsl);
        log.debug("dsl: {}", dsl);
        Response response = restClient.performRequest(request);
        return getResponseJsonObj(response);
    }

    private static JSONObject getResponseJsonObj(Response response) throws IOException {
        InputStream in = response.getEntity().getContent();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
        byte[] bytes = new byte[1024];
        int bytesRead = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while ((bytesRead = bufferedInputStream.read(bytes)) != -1) {
            stringBuilder.append(new String(bytes, 0, bytesRead));
        }
        return OrgJsonUtil.jsonStrToJsonObject(stringBuilder.toString());
    }
}
