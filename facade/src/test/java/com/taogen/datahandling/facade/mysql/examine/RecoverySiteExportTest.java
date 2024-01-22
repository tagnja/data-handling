package com.taogen.datahandling.facade.mysql.examine;

import com.taogen.commons.io.FileUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author taogen
 */
@Disabled
@Slf4j
public class RecoverySiteExportTest extends ExportBaseTest {
    @ParameterizedTest
    @Disabled
    @CsvSource({
            "18"
    })
    void getSitesOfGroup(Integer groupId) throws IOException {
        Map<String, Object> groupMap = jdbcTemplate.queryForMap("select name from recovery_group where id = " + groupId);
        Object groupName = groupMap.get("name");
        String sql = "select rs.name as '站点名称', (case when rs.type = '2' then '微博' when rs.type = '3' then '公众号' else '网站' end) as '类型', host as '站点'  from recovery_site as rs right join recovery_site_group as rsg on rs.id=rsg.site_id where rsg.group_id = " + groupId;
        SqlQueryParam sqlQueryParam = new SqlQueryParam();
        sqlQueryParam.setSql(sql);
        LabelAndData labelAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
        String outputPath = getExportDirPath() + FileUtils.appendDateTimeToFileName(groupName + "_站点.xlsx");
        excelWriter.writeLabelAndDataToExcel(labelAndData, outputPath);
    }

    @ParameterizedTest
    @Disabled
    @CsvSource({
            "AA、BB, 、",
    })
    void findSiteNames(String keywords, String delimiter) {
        String[] keywordArray = keywords.split(delimiter);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String siteName : keywordArray) {
            log.info("site name: {}", siteName);
            String sql = "select id, name from recovery_site where name like '%" + siteName + "%'";
            log.debug("sql is {}", sql);
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            resultList.addAll(result);
            log.info("site name result: \n{}", result.stream().map(Objects::toString).collect(Collectors.joining("\r\n")));
        }
        log.info("total sites: \n{}", resultList.stream().map(Objects::toString).collect(Collectors.joining("\r\n")));
        log.info("site size is {}", resultList.size());
        log.info("total site ids: {}", resultList.stream().map(item -> item.get("id")).map(Objects::toString).collect(Collectors.joining(",")));
    }
}
