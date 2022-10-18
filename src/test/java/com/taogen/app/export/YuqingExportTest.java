package com.taogen.app.export;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Taogen
 */
@Slf4j
@Disabled
public class YuqingExportTest extends ExportBaseTest{

    @Test
    @Disabled
    void exportWeiboSubscribeKeywords() {
        String sql = "select name from weibo_sub_keyword where type = 1";
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
        log.info("keyword size is: {}", resultList.size());
        String keywordsJoinWithOr = resultList.stream().map(item -> item.get("name"))
                .map(Object::toString)
                .collect(Collectors.joining(" or "));
        System.out.println(keywordsJoinWithOr);
    }
}
