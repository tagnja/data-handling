package com.taogen.datahandling.facade.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author taogen
 */
@AllArgsConstructor
@Getter
public enum EsField {
    ID("ID", "id", "id"),
    TITLE("标题", "title", "title"),
    CONTENT("内容", "content", "content"),
    AUTHOR("作者", "author", "author"),
    PUB_TIME("发布时间", "pub_time", "pub_time"),
    SOURCE_URL("链接", "source_url", "source_url"),
    HOST_NAME("站点", "host", "host_name"),
    IP_REGION("IP归属地", "remark1", "ip_region"),
    SOURCE_NAME("来源", "source_id", "source_name"),
    CHECK_IN_AREA("签到地点", "content", "check_in_area"),
    LEVEL_NAME("属性", "level_id", "level_name");

    private String LabelName;
    private String queryField;
    private String exportField;
}
