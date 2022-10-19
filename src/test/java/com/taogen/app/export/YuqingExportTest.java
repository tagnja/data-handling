package com.taogen.app.export;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    @Disabled
    void exportWeiboSubscribeKeywordsByFolders() {
        List<Integer> totalFolderIds = new ArrayList<>();
        boolean onlySubscribed = false;
        List<String> folderNames = Arrays.asList("AA", "BB", "CC");
        String selectFolderSql = "select id, name from weibo_sub_keyword where type = 0 and name in (" + folderNames.stream().map(item -> "'" + item + "'").collect(Collectors.joining(",")) + ")";
        log.debug("selectFolderSql is {}", selectFolderSql);
        List<Map<String, Object>> rootFolders = jdbcTemplate.queryForList(selectFolderSql);
        log.debug("rootFolders: \n{}", rootFolders);
        assertEquals(folderNames.size(), rootFolders.size());
        List<Integer> rootFolderIds = rootFolders.stream().map(item -> item.get("id")).map(Objects::toString).map(Integer::valueOf).collect(Collectors.toList());
        totalFolderIds.addAll(rootFolderIds);
        totalFolderIds.addAll(getDescendantIds(rootFolderIds));
        String selectKeywordsSql = "select id, name from weibo_sub_keyword where type = 1 and parent_id in ("+totalFolderIds.stream().map(Objects::toString).collect(Collectors.joining(","))+")";
        if (onlySubscribed) {
            selectKeywordsSql += "and (collect_sub_sensitive = 1 or collect_sub_all = 1 or small_video_sensitive = 1) ";
        }
        log.debug("selectKeywordsSql is {}", selectKeywordsSql);
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(selectKeywordsSql);
        log.info("keyword size is: {}", resultList.size());
        String keywordsJoinWithOr = resultList.stream().map(item -> item.get("name"))
                .map(Object::toString)
                .collect(Collectors.joining(" or "));
        System.out.println(keywordsJoinWithOr);
    }

    public Set<Integer> getDescendantIds(List<Integer> parentIds) {
        List<Object> descendantIds = new ArrayList<>();
        String selectChildFolderSql = "select id, name from weibo_sub_keyword where type = 0 and parent_id in ("+parentIds.stream().map(Objects::toString).collect(Collectors.joining(","))+")";
        log.debug("selectFolderSql is {}", selectChildFolderSql);
        List<Map<String, Object>> childFolders = jdbcTemplate.queryForList(selectChildFolderSql);
        while (!CollectionUtils.isEmpty(childFolders)) {
            List<Object> childrenIds = childFolders.stream().map(item -> item.get("id")).collect(Collectors.toList());
            descendantIds.addAll(childrenIds);
            selectChildFolderSql = "select id, name from weibo_sub_keyword where type = 0 and parent_id in ("+childrenIds.stream().map(Objects::toString).collect(Collectors.joining(","))+")";
            log.debug("selectFolderSql is {}", selectChildFolderSql);
            childFolders = jdbcTemplate.queryForList(selectChildFolderSql);
        }
        return descendantIds.stream()
                .map(Objects::toString)
                .filter(Objects::nonNull)
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
    }
}
