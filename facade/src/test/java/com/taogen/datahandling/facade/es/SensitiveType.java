package com.taogen.datahandling.facade.es;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Taogen
 */
public enum SensitiveType {
    SENSITIVE("1", "敏感"),

    NONSENSITIVE("-1", "非敏感");

    private String levelId;

    private String levelName;

    SensitiveType(String levelId, String levelName) {
        this.levelId = levelId;
        this.levelName = levelName;
    }

    public static SensitiveType fromLevelId(String levelId) {
        if (levelId == null) {
            return null;
        }
        Optional<SensitiveType> sensitiveType = Arrays.asList(SensitiveType.values())
                .stream()
                .filter(item -> item.getLevelId().equals(levelId))
                .findFirst();
        return sensitiveType.isPresent() ? sensitiveType.get() : null;
    }

    public String getLevelId() {
        return levelId;
    }

    public String getLevelName() {
        return levelName;
    }
}
