package com.taogen.datahandling.facade.es.common;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author taogen
 */
@Slf4j
public class DslBuilder {

    public static String buildForSearch(List<EsFieldInfo> queryFields,
                                        Map<EsFieldInfo, Object[]> queryConditions,
                                        String keywordExpression,
                                        Integer size,
                                        String orderField,
                                        SortOrder sortOrder) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (queryFields != null) {
            List<String> fetchFields = queryFields.stream().map(EsFieldInfo::getQueryField).distinct().collect(Collectors.toList());
            searchSourceBuilder.fetchSource(fetchFields.toArray(new String[0]), null);
        }
        if (size != null) {
            searchSourceBuilder.size(size);
        }
        if (orderField != null && sortOrder != null) {
            searchSourceBuilder.sort(orderField, sortOrder);
        }
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 非垃圾数据（默认条件，和舆情搜索保持一致）
        boolQueryBuilder.must(QueryBuilders.termQuery(EsField.STATUS, "0"));
        BoolQueryBuilder keywordBoolQuery = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExpression);
        if (keywordBoolQuery != null) {
            boolQueryBuilder.must(keywordBoolQuery);
        }
        if (queryConditions != null) {
            for (Map.Entry<EsFieldInfo, Object[]> entry : queryConditions.entrySet()) {
                EsFieldInfo esFieldInfo = entry.getKey();
                Object[] values = entry.getValue();
                boolQueryBuilder.must(esFieldInfo.getQueryFunction().apply(values));
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        log.debug("searchSourceBuilder: {}", searchSourceBuilder);
        return searchSourceBuilder.toString();
    }

    public static String buildForCount(Map<EsFieldInfo, Object[]> queryConditions,
                                       String keywordExpression) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 非垃圾数据（默认条件，和舆情搜索保持一致）
        boolQueryBuilder.must(QueryBuilders.termQuery(EsField.STATUS, "0"));
        BoolQueryBuilder keywordBoolQuery = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExpression);
        if (keywordBoolQuery != null) {
            boolQueryBuilder.must(keywordBoolQuery);
        }
        if (queryConditions != null) {
            for (Map.Entry<EsFieldInfo, Object[]> entry : queryConditions.entrySet()) {
                EsFieldInfo esFieldInfo = entry.getKey();
                Object[] values = entry.getValue();
                boolQueryBuilder.must(esFieldInfo.getQueryFunction().apply(values));
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        log.debug("searchSourceBuilder: {}", searchSourceBuilder);
        return searchSourceBuilder.toString();
    }

}
