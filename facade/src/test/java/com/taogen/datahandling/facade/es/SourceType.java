package com.taogen.datahandling.facade.es;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Taogen
 */
public enum SourceType {
    NEWS("1", "新闻"),
    BBS("2", "论坛"),
    BLOG("3", "博客"),
    VIDEO("4", "视频"),
    WEIBO("5", "微博"),
    SELF_MEDIA("6", "自媒体"),
    MOBILE("7", "手机"),
    OVERSEA("8", "海外"),
    QQ_GROUP("9", "QQ群");

    private String sourceId;
    private String sourceName;

    SourceType(String sourceId, String sourceName) {
        this.sourceId = sourceId;
        this.sourceName = sourceName;
    }

    public static SourceType fromSourceId(String sourceId) {
        if (sourceId == null) {
            return null;
        }
        Optional<SourceType> sourceType = Arrays.asList(SourceType.values())
                .stream()
                .filter(item -> item.getSourceId().equals(sourceId))
                .findFirst();
        return sourceType.isPresent() ? sourceType.get() : null;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

}
