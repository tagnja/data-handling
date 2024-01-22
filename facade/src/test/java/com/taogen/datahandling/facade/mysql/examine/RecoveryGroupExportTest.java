package com.taogen.datahandling.facade.mysql.examine;

import com.taogen.commons.datatypes.string.StringUtils;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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


    @Test
    @Disabled
    void findGroupNamesByCheckStatusEnable() {
        String sql = "select id, name from recovery_group where check_status = 0";
        log.debug("sql is {}", sql);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        log.info("group name result: \n{}", result.stream().map(Objects::toString).collect(Collectors.joining("\r\n")));
        log.info("total groups: \n{}", result.stream().map(Objects::toString).collect(Collectors.joining("\r\n")));
        log.info("group size is {}", result.size());
        log.info("total group ids: {}", result.stream().map(item -> item.get("id")).map(Objects::toString).collect(Collectors.joining(",")));
        // result: 18,19,42,56,80,84,103,126,136,157,175,178,181,195,199,200,201,214,226,234,238,241,264,273,274,275,276,277,278,279,290
    }

    @Test
    public void test_findGroupIds() {
        List<Integer> allGroupIds = findGroupIds(null);
        assertNotNull(allGroupIds);
        assertFalse(allGroupIds.isEmpty());
        // -1, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 24, 25, 27, 28, 29, 30, 31, 32, 33, 34, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 74, 75, 76, 77, 78, 79, 80, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 93, 94, 96, 97, 99, 101, 103, 104, 105, 106, 108, 109, 111, 112, 113, 114, 116, 117, 118, 120, 122, 124, 125, 126, 127, 128, 130, 131, 132, 133, 134, 136, 137, 138, 139, 140, 141, 142, 145, 146, 148, 149, 150, 151, 153, 155, 157, 158, 160, 161, 163, 164, 167, 168, 169, 170, 172, 173, 175, 178, 180, 181, 182, 183, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 210, 211, 212, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 255, 256, 257, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293, 294, 295, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314
        log.debug(allGroupIds.toString());
        log.info("group size: {}", allGroupIds.size());
        List<Integer> groupIds = findGroupIds("check_status = 0");
        assertNotNull(groupIds);
        assertFalse(groupIds.isEmpty());
    }

    private List<Integer> findGroupIds(String predicate) {
        StringBuilder sql = new StringBuilder()
                .append("select id from recovery_group");
        if (StringUtils.isNotEmpty(predicate)) {
            sql.append(" where ").append(predicate);
        }
        sql.append(" order by id asc");
        List<Integer> groupIds = jdbcTemplate.queryForList(sql.toString(), Integer.class);
        return groupIds;
    }
}
