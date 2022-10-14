package com.taogen.app.export;

import com.taogen.app.functions.conversion.datasystems.mysql.vo.SqlQueryParam;
import com.taogen.app.functions.conversion.datasystems.mysql.vo.TableLabelAndData;
import com.taogen.commons.io.DirectoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Taogen
 */
@Slf4j
@Disabled
public class RecoveryDataExportTest extends ExportBaseTest {

    @BeforeEach
    void beforeEach() {
        showConfig();
    }

    @Test
    void splitModifyAndJoin_string_test1() {
        String s = "AA|BB|CC";
        String delimiter = "\\|";
        Function<String, String> function = item -> String.format("title like \"%%%s%%\" or content like \"%%%s%%\"", item, item);
        String handledText = textModifier.splitModifyAndJoin(s, delimiter, function, " or ");
    }

    @Test
    @Disabled
    void modifyRows_appendContainsSpecifiedWordToRows_test() throws IOException, URISyntaxException {
        String keywords = "AA、BB、CC、DD";
        String delimiter = "、";
        String inputFilePath = DirectoryUtils.getUserHomeDir() + "\\Desktop\\test.xlsx";
        int appendToColumn = 8;
        // start to call modifyWorkbook
        String[] names = keywords.split(delimiter);
        DataFormatter formatter = new DataFormatter();
        Consumer<Row> rowsModifyConsumer = row -> {
            Cell titleCell = row.getCell(1);
            String title = formatter.formatCellValue(titleCell);
            System.out.println("title: " + title);
            Cell contentCell = row.getCell(2);
            String content = formatter.formatCellValue(contentCell);
            System.out.println("content: " + content);
            List<String> containsKeywords = Arrays.stream(names)
                    .filter(item -> title.contains(item) || content.contains(item))
                    .collect(Collectors.toList());
            System.out.println("keywords: " + containsKeywords);
            Cell cell = row.createCell(appendToColumn);
            cell.setCellValue(String.join(delimiter, containsKeywords));
        };
        String outputFilePath = excelModifier.modifyRows(inputFilePath, rowsModifyConsumer);
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
    @Disabled
    void findGroupNames() {
        String keywords = "Apple、Orange";
        String delimiter = "、";
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
    }

    @Test
    void exportFromAllTableToSingleExcel() throws IOException {
        long startTime = System.currentTimeMillis();
        List<String> tableList = getTableList();
//        List<Integer> groupIds = Arrays.asList(18, 19, 42, 56, 80, 84, 103, 126, 136, 157, 175, 178, 181, 195, 199, 200, 201, 214, 226, 234, 238, 241, 264, 273, 274, 275, 276, 277, 278, 279, 290);
        List<Integer> groupIds = Arrays.asList(80, 19);

        String sql = "select rd.id as \"ID\", rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.gmt_create as \"入库时间\" ,rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\"\n" +
                "from app_wzjc.${table_name} rd \n" +
                "left join recovery_group rg on rg.id = rd.group_id \n" +
                "left join recovery_site rs on rs.id = rd.site_id\n" +
                "where group_id in (${group_id}) and (${keyword_predicate})";
        String keywords = "张三、李四";
        String delimiter = "、";
        String keywordPredicate = getKeywordPredicate(keywords, delimiter);
        sql = sql.replace("${keyword_predicate}", keywordPredicate);
        TableLabelAndData resultLabelAndData = new TableLabelAndData(new ArrayList<>(), new ArrayList<>());
        for (Integer groupId : groupIds) {
//            String groupIds = "18";
            Map<String, Object> groupMap = jdbcTemplate.queryForMap("select name from recovery_group where id = " + groupId);
            Object groupName = groupMap.get("name");
            String sqlAddGroup = sql.replace("${group_id}", groupId.toString());
            for (String table : tableList) {
                String queryTableSql = sqlAddGroup.replace("${table_name}", table);
                log.debug("queryTableSql is: {}", queryTableSql);
                SqlQueryParam sqlQueryParam = new SqlQueryParam();
                sqlQueryParam.setSql(queryTableSql);
                sqlQueryParam.setBatchFetch(false);
                TableLabelAndData tableLabelsAndData = mysql2ExcelConverter.getTableLabelsAndData(sqlQueryParam);
                resultLabelAndData.setLabels(tableLabelsAndData.getLabels());
                resultLabelAndData.getValuesList().addAll(tableLabelsAndData.getValuesList());
            }
        }
        String outputDir = DirectoryUtils.getUserHomeDir() + "\\Desktop\\export";
        String outputFileName = new StringBuilder()
                .append("审核-数据-")
                .append(System.currentTimeMillis())
                .append(".xlsx")
                .toString();
        String outputFilePath = mysql2ExcelConverter.writeTableLabelAndDataToExcel(resultLabelAndData,
                outputDir, outputFileName);
        String resultOutputFilePath = generateAppendKeywordsExcel(outputFilePath, keywords, delimiter);
        log.info("Elapsed time: {} ms", System.currentTimeMillis() - startTime);
    }

    @Test
    void exportFromAllTableSeparateExcelByGroup() throws IOException {
        long startTime = System.currentTimeMillis();
        List<String> tableList = getTableList();
//        List<Integer> groupIds = Arrays.asList(18, 19, 42, 56, 80, 84, 103, 126, 136, 157, 175, 178, 181, 195, 199, 200, 201, 214, 226, 234, 238, 241, 264, 273, 274, 275, 276, 277, 278, 279, 290);
        List<Integer> groupIds = Arrays.asList(80);

        String sql = "select rd.id as \"ID\", rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.gmt_create as \"入库时间\" ,rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\"\n" +
                "from app_wzjc.${table_name} rd \n" +
                "left join recovery_group rg on rg.id = rd.group_id \n" +
                "left join recovery_site rs on rs.id = rd.site_id\n" +
                "where group_id in (${group_id}) and (${keyword_predicate})";
        String keywords = "张三、李四";
        String delimiter = "、";
        String keywordPredicate = getKeywordPredicate(keywords, delimiter);
        sql = sql.replace("${keyword_predicate}", keywordPredicate);
        for (Integer groupId : groupIds) {
            Map<String, Object> groupMap = jdbcTemplate.queryForMap("select name from recovery_group where id = " + groupId);
            Object groupName = groupMap.get("name");
            String sqlAddGroup = sql.replace("${group_id}", groupId.toString());
            TableLabelAndData resultLabelAndData = new TableLabelAndData(new ArrayList<>(), new ArrayList<>());
            for (String table : tableList) {
                String queryTableSql = sqlAddGroup.replace("${table_name}", table);
                log.info("queryTableSql is: {}", queryTableSql);
                SqlQueryParam sqlQueryParam = new SqlQueryParam();
                sqlQueryParam.setSql(queryTableSql);
                sqlQueryParam.setBatchFetch(false);
                TableLabelAndData tableLabelsAndData = mysql2ExcelConverter.getTableLabelsAndData(sqlQueryParam);
                resultLabelAndData.setLabels(tableLabelsAndData.getLabels());
                resultLabelAndData.getValuesList().addAll(tableLabelsAndData.getValuesList());
            }
            String outputDir = DirectoryUtils.getUserHomeDir() + "\\Desktop\\export";
            String outputFileName = new StringBuilder()
                    .append("审核-数据-")
                    .append(Objects.toString(groupName))
                    .append(System.currentTimeMillis())
                    .append(".xlsx")
                    .toString();
            String outputFilePath = mysql2ExcelConverter.writeTableLabelAndDataToExcel(resultLabelAndData,
                    outputDir, outputFileName);
            String resultOutputFilePath = generateAppendKeywordsExcel(outputFilePath, keywords, delimiter);
            log.info("Elapsed time: {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private List<String> getTableList() {
        String selectTableSql = "select\n" +
                "TABLE_SCHEMA as tableSchema,\n" +
                "TABLE_NAME as tableName,\n" +
                "TABLE_COMMENT as tableComment,\n" +
                "CREATE_TIME as createTime,\n" +
                "UPDATE_TIME as updateTime\n" +
                "from information_schema.tables\n" +
                "where TABLE_SCHEMA=\"app_wzjc\" and \n" +
                "(TABLE_NAME like \"_tmp_recovery_data%\" or TABLE_NAME = \"recovery_data\" or TABLE_NAME = \"recovery_data_20210929\") and TABLE_NAME not in (\"_tmp_recovery_data_copy1\", \"_tmp_recovery_data_wechat\")";
        log.debug("select table sql is {}", selectTableSql);
        List<Map<String, Object>> tableMapList = jdbcTemplate.queryForList(selectTableSql);
        List<String> tableNameList = tableMapList.stream()
                .map(item -> item.get("tableName"))
                .map(Objects::toString)
                .collect(Collectors.toList());
        log.info("table name list: \r\n{}", tableNameList.stream().collect(Collectors.joining("\r\n")));
        log.info("table name list size is {}", tableNameList.size());
        assertEquals(20, tableNameList.size());
        return tableNameList;
    }

    private String generateAppendKeywordsExcel(String inputFilePath,
                                               String keywords,
                                               String delimiter) throws IOException {
        int appendToColumn = 8;
        // start to call modifyWorkbook
        String[] names = keywords.split(delimiter);
        DataFormatter formatter = new DataFormatter();
        Consumer<Row> rowsModifyConsumer = row -> {
            Cell titleCell = row.getCell(1);
            String title = formatter.formatCellValue(titleCell);
            log.trace("title: " + title);
            Cell contentCell = row.getCell(2);
            String content = formatter.formatCellValue(contentCell);
            log.trace("content: " + content);
            List<String> containsKeywords = Arrays.stream(names)
                    .filter(item -> title.contains(item) || content.contains(item))
                    .collect(Collectors.toList());
            log.trace("keywords: " + containsKeywords);
            Cell cell = row.createCell(appendToColumn);
            cell.setCellValue(String.join(delimiter, containsKeywords));
        };
        String outputFilePath = excelModifier.modifyRows(inputFilePath, rowsModifyConsumer);
        return outputFilePath;
    }

    private String getKeywordPredicate(String keywords, String delimiter) {
        Function<String, String> function = item -> String.format("title like \"%%%s%%\" or content like \"%%%s%%\"", item, item);
        return textModifier.splitModifyAndJoin(keywords, delimiter, function, " or ");
    }
}
