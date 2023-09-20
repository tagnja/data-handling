package com.taogen.datahandling.es.service.impl;

import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.es.service.EsReader;
import com.taogen.datahandling.es.util.LowLevelRestClientUtils;
import com.taogen.datahandling.es.vo.DslQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author taogen
 */
@Component
@Slf4j
public class EsReaderImpl implements EsReader {
    @Override
    public LabelAndData readAll(RestClient restClient, DslQueryParam dslQueryParam) throws IOException {
        long startTime = System.currentTimeMillis();
        List<JSONObject> itemJsonList = LowLevelRestClientUtils.scrollQuery(restClient, dslQueryParam.getIndex(), dslQueryParam.getDsl());
        LabelAndData labelAndData = convertToLabelAndData(itemJsonList, dslQueryParam);
        long endTime = System.currentTimeMillis();
        log.debug("readAll cost {} ms", endTime - startTime);
        return labelAndData;
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
        log.debug("valuesList size: {}", valuesList.size());
        labelAndData.setValuesList(valuesList);
        return labelAndData;
    }
}
