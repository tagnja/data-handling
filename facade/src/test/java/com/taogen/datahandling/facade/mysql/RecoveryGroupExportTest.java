package com.taogen.datahandling.facade.mysql;

import com.taogen.datahandling.facade.base.ExportBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author taogen
 */
@Slf4j
public class RecoveryGroupExportTest extends ExportBaseTest {
    @Test
    public void getAllGroup() {
        String sql = "select id, name from recovery_group";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
//        log.info("group name result: \n{}", result.stream().map(Objects::toString).collect(Collectors.joining("\r\n")));
        System.out.println("insert into aijiaodui_account ");
        System.out.println("(component_company , component_contact , component_phone, component_mail , component_app_id, component_app_secret , component_words_count, component_start_time , component_end_time , component_status , component_type, component_create_time , component_modified_time ,component_surplus_words_count )");
        System.out.println("values");
        for (Map<String, Object> item : result) {
            System.out.print("('a', 'a', 'a', 'a', '" + item.get("id") + "', 'Sytc123456......', 2047483647, 1700909422000, 2647680622000, 1, 2, 1700909422000, 1700909422000, 2047483647)");
            if (result.indexOf(item) != result.size() - 1) {
                System.out.println(",");
            }
        }
    }

    @ParameterizedTest
    @Disabled
    @CsvSource({
            "AA、BB, 、",
    })
    void findGroupNames(String keywords, String delimiter) {
        String[] keywordArray = keywords.split(delimiter);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String groupName : keywordArray) {
            log.info("group name: {}", groupName);
            String sql = "select id, name from recovery_group where name like '%" + groupName + "%'";
            log.debug("sql is {}", sql);
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            resultList.addAll(result);
            log.info("group name result: \n{}", result.stream().map(Objects::toString).collect(Collectors.joining("\r\n")));
        }
        log.info("total groups: \n{}", resultList.stream().map(Objects::toString).collect(Collectors.joining("\r\n")));
        log.info("group size is {}", resultList.size());
        log.info("total group ids: {}", resultList.stream().map(item -> item.get("id")).map(Objects::toString).collect(Collectors.joining(",")));
        resultList.forEach(item -> {
            Object groupId = item.get("id");
            String selectSiteSql = "select b.id, b.name from recovery_site_group as a left join recovery_site as b on a.site_id = b.id where group_id = " + groupId;
            List<Map<String, Object>> siteResult = jdbcTemplate.queryForList(selectSiteSql);
            log.info("group name is {}", item.get("name"));
            log.info("site name result: \n{}", siteResult.stream().map(i -> i.get("name")).map(Objects::toString).collect(Collectors.joining("\r\n")));
        });
    }

}
