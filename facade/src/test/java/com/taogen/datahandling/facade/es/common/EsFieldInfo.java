package com.taogen.datahandling.facade.es.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.function.Function;

/**
 * @author taogen
 */
@AllArgsConstructor
@Getter
public enum EsFieldInfo {

    ID("ID", "id", "id",
            value -> QueryBuilders.termsQuery("id", value)),
    TITLE("标题", "title", "title",
            value -> QueryBuilders.wildcardQuery("title", (String) value[0])),
    CONTENT("内容", "content", "content",
            value -> QueryBuilders.wildcardQuery("content", (String) value[0])),
    AUTHOR("作者", "author", "author",
            value -> QueryBuilders.wildcardQuery("author", (String) value[0])),
    PUB_TIME("发布时间", "pub_time", "pub_time",
            value -> QueryBuilders.rangeQuery("pub_time").gte(value[0]).lte(value[1])),
    SOURCE_URL("链接", "source_url", "source_url",
            value -> QueryBuilders.wildcardQuery("source_url", (String) value[0])),
    HOST_NAME("站点", "host", "host_name",
            value -> QueryBuilders.termsQuery("host", value)),
    IP_REGION("IP归属地", "remark1", "ip_region",
            value -> QueryBuilders.wildcardQuery("remark1", (String) value[0])),
    SOURCE_NAME("来源", "source_id", "source_name",
            value -> QueryBuilders.termsQuery("source_id", value)),
    CHECK_IN_AREA("签到地点", "content", "check_in_area",
            value -> QueryBuilders.wildcardQuery("content", (String) value[0])),
    LEVEL_NAME("属性", "level_id", "level_name",
            value -> QueryBuilders.termsQuery("level_id", value)),
    DEP("客户标签", "dep", "dep",
            value -> QueryBuilders.termsQuery("dep_id", value)),
    ;

    private String LabelName;
    private String queryField;
    private String exportField;
    //    private QueryBuilder queryFunction;
    private Function<Object[], QueryBuilder> queryFunction;
}
