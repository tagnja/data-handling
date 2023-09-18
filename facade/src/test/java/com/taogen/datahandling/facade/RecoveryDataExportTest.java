package com.taogen.datahandling.facade;

import com.taogen.commons.datatypes.string.StringUtils;
import com.taogen.commons.io.DirectoryUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Taogen
 */
@Slf4j
@Disabled
public class RecoveryDataExportTest extends ExportBaseTest {

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
        String inputFilePath = DirectoryUtils.getUserHomeDir() + File.separator + "export" + File.separator + "test.xlsx";
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
        String outputFilePath = excelModifier.modifyRows(inputFilePath, 0, rowsModifyConsumer);
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
        resultList.forEach(item -> {
            Object groupId = item.get("id");
            String selectSiteSql = "select b.id, b.name from recovery_site_group as a left join recovery_site as b on a.site_id = b.id where group_id = " + groupId;
            List<Map<String, Object>> siteResult = jdbcTemplate.queryForList(selectSiteSql);
            log.info("group name is {}", item.get("name"));
            log.info("site name result: \n{}", siteResult.stream().map(i -> i.get("name")).map(Objects::toString).collect(Collectors.joining("\r\n")));
        });
    }

    @Test
    void exportFromAllTableToSingleExcel() throws IOException {
        long startTime = System.currentTimeMillis();
        List<String> tableList = getTableList();
//        List<Integer> groupIds = Arrays.asList(18, 19, 42, 56, 80, 84, 103, 126, 136, 157, 175, 178, 181, 195, 199, 200, 201, 214, 226, 234, 238, 241, 264, 273, 274, 275, 276, 277, 278, 279, 290);
        List<Integer> groupIds = Arrays.asList(80, 19);

        // 没有 ID 和 入库时间
        String sql = "select rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\"\n" +
                "from app_wzjc.${table_name} rd \n" +
                "left join recovery_group rg on rg.id = rd.group_id \n" +
                "left join recovery_site rs on rs.id = rd.site_id\n" +
                "where group_id in (${group_id}) and (${keyword_predicate})";
        // 有 ID 和 入库时间
//        String sql = "select rd.id as \"ID\", rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.gmt_create as \"入库时间\" ,rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\", content_correct_words\n" +
//                "from app_wzjc.${table_name} rd \n" +
//                "left join recovery_group rg on rg.id = rd.group_id \n" +
//                "left join recovery_site rs on rs.id = rd.site_id\n" +
//                "where group_id in (${group_id}) and (${keyword_predicate})";
//        String sql = "select rd.id as \"ID\", rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.gmt_create as \"入库时间\" ,rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\", content_correct_words\n" +
//                "from app_wzjc.${table_name} rd \n" +
//                "left join recovery_group rg on rg.id = rd.group_id \n" +
//                "left join recovery_site rs on rs.id = rd.site_id\n" +
//                "where group_id in (${group_id}) and (content_correct_words like '%\"level\":1%' or content_correct_words like '%\"level\":3%' or content_correct_words like '%\"level\":-3%')";
        String keywords = "张三、李四";
        String delimiter = "\\|"; // '|' need to add backslash '\\|'
        String newDelimiter = "、";
        keywords = textModifier.updateDelimiter(keywords, delimiter, newDelimiter);
        delimiter = newDelimiter;
        String keywordPredicate = getKeywordPredicate(keywords, delimiter);
        sql = sql.replace("${keyword_predicate}", keywordPredicate);
        LabelAndData resultLabelAndData = new LabelAndData(new ArrayList<>(), new ArrayList<>());
        for (Integer groupId : groupIds) {
            Map<String, Object> groupMap = jdbcTemplate.queryForMap("select name from recovery_group where id = " + groupId);
            Object groupName = groupMap.get("name");
            String sqlAddGroup = sql.replace("${group_id}", groupId.toString());
            for (String table : tableList) {
                String queryTableSql = sqlAddGroup.replace("${table_name}", table);
                log.debug("queryTableSql is: {}", queryTableSql);
                SqlQueryParam sqlQueryParam = new SqlQueryParam();
                sqlQueryParam.setSql(queryTableSql);
                sqlQueryParam.setBatchFetch(false);
                LabelAndData tableLabelsAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
                resultLabelAndData.setLabels(tableLabelsAndData.getLabels());
                resultLabelAndData.getValuesList().addAll(tableLabelsAndData.getValuesList());
            }
        }
        String outputDir = DirectoryUtils.getUserHomeDir() + File.separator + "export";
        String outputFileName = new StringBuilder()
                .append("审核-数据-")
                .append(System.currentTimeMillis())
                .append(".xlsx")
                .toString();
        String outputFilePath = excelWriter.writeLabelAndDataToExcel(resultLabelAndData,
                outputDir + File.separator + outputFileName);
        // 没有 ID 和 入库时间
        int appendToColumn = 6;
        int titleColumn = 0;
        int contentColumn = 1;
        // 有 ID 和 入库时间
//        int appendToColumn = 8;
//        int titleColumn = 1;
//        int contentColumn = 2;
        String resultOutputFilePath = generateAppendKeywordsExcel(
                outputFilePath, keywords, delimiter, appendToColumn, titleColumn, contentColumn);
        log.info("output file path: {}", resultOutputFilePath);
        log.info("Elapsed time: {} ms", System.currentTimeMillis() - startTime);
    }

    @Test
    void exportFromAllTableSeparateExcelByGroup() throws IOException {
        long startTime = System.currentTimeMillis();
        List<String> tableList = getTableList();
        List<Integer> groupIds = Arrays.asList(80);
        // 是否隐藏为开启的所有分组（不含admin）。30个关键词导出耗时：15.2 min。60个关键词耗时：27 min。
//        List<Integer> groupIds = Arrays.asList(18, 19, 42, 56, 80, 84, 103, 126, 136, 157, 175, 178, 181, 195, 199, 200, 201, 214, 226, 234, 238, 241, 264, 273, 274, 275, 276, 277, 278, 279, 290);
//        List<Integer> groupIds = Arrays.asList(238);
//        List<Integer> groupIds = findGroupIds(null);
//        groupIds.removeIf(item -> item.equals(-1));
        log.info("export groupIds size: {}", groupIds.size());

        // 没有 ID 和 入库时间
        String sql = "select rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\"\n" +
                "from app_wzjc.${table_name} rd \n" +
                "left join recovery_group rg on rg.id = rd.group_id \n" +
                "left join recovery_site rs on rs.id = rd.site_id\n" +
                "where group_id in (${group_id}) and (${keyword_predicate})";
//        sql += "and rd.pubtime between '2022-10-01 00:00:00' and '2023-01-06 23:59:59'";
        // 有 ID 和 入库时间
//        String sql = "select rd.id as \"ID\", rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.gmt_create as \"入库时间\" ,rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\"\n" +
//                "from app_wzjc.${table_name} rd \n" +
//                "left join recovery_group rg on rg.id = rd.group_id \n" +
//                "left join recovery_site rs on rs.id = rd.site_id\n" +
//                "where group_id in (${group_id}) and (${keyword_predicate})";
        String keywords = "张三、李四";
        String delimiter = "、"; // '|' need to add a backslash '\\|'
        String newDelimiter = "---";
        keywords = textModifier.updateDelimiter(keywords, delimiter, newDelimiter);
        delimiter = newDelimiter;
        String keywordPredicate = getKeywordPredicate(keywords, delimiter);
        sql = sql.replace("${keyword_predicate}", keywordPredicate);
        for (Integer groupId : groupIds) {
            Map<String, Object> groupMap = jdbcTemplate.queryForMap("select name from recovery_group where id = " + groupId);
            Object groupName = groupMap.get("name");
            String sqlAddGroup = sql.replace("${group_id}", groupId.toString());
            LabelAndData resultLabelAndData = new LabelAndData(new ArrayList<>(), new ArrayList<>());
            for (String table : tableList) {
                String queryTableSql = sqlAddGroup.replace("${table_name}", table);
                log.info("queryTableSql is: {}", queryTableSql);
                SqlQueryParam sqlQueryParam = new SqlQueryParam();
                sqlQueryParam.setSql(queryTableSql);
                sqlQueryParam.setBatchFetch(false);
                LabelAndData tableLabelsAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
                resultLabelAndData.setLabels(tableLabelsAndData.getLabels());
                resultLabelAndData.getValuesList().addAll(tableLabelsAndData.getValuesList());
            }
            // don't export empty data recovery group
            if (resultLabelAndData.getValuesList().isEmpty()) {
                continue;
            }
            String outputDirPath = DirectoryUtils.getUserHomeDir() + File.separator + "export";
            File dir = new File(outputDirPath);
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdirs();
            }
            String outputFileName = new StringBuilder()
                    .append("审核-数据-")
                    .append(Objects.toString(groupName))
                    .append(System.currentTimeMillis())
                    .append(".xlsx")
                    .toString();
            String outputFilePath = excelWriter.writeLabelAndDataToExcel(resultLabelAndData,
                    outputDirPath + File.separator + outputFileName);
            System.out.println("output file path: " + outputFilePath);
            // 没有 ID 和 入库时间
            int appendToColumn = 6;
            int titleColumn = 0;
            int contentColumn = 1;
            // 有 ID 和 入库时间
//            int appendToColumn = 8;
//            int titleColumn = 1;
//            int contentColumn = 2;

            // 是否标注关键词
            String resultOutputFilePath = generateAppendKeywordsExcel(
                    outputFilePath, keywords, delimiter, appendToColumn, titleColumn, contentColumn);
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
        assertEquals(21, tableNameList.size());
        return tableNameList;
    }

    private String generateAppendKeywordsExcel(String inputFilePath,
                                               String keywords,
                                               String delimiter,
                                               int appendToColumn,
                                               int titleColumn,
                                               int contentColumn) throws IOException {
        // start to call modifyWorkbook
        List<String> names = Arrays.stream(keywords.split(delimiter))
                .map(String::trim)
                .collect(Collectors.toList());
        DataFormatter formatter = new DataFormatter();
        Consumer<Row> rowsModifyConsumer = row -> {
            Cell titleCell = row.getCell(titleColumn);
            String title = formatter.formatCellValue(titleCell);
            log.trace("title: " + title);
            Cell contentCell = row.getCell(contentColumn);
            String content = formatter.formatCellValue(contentCell);
            log.trace("content: " + content);
            List<String> containsKeywords = names.stream()
                    .filter(item -> title.contains(item) || content.contains(item))
                    .collect(Collectors.toList());
            log.trace("keywords: " + containsKeywords);
            Cell cell = row.createCell(appendToColumn);
            cell.setCellValue(String.join(delimiter, containsKeywords));
        };
        String outputFilePath = excelModifier.modifyRows(inputFilePath, 0, rowsModifyConsumer);
        return outputFilePath;
    }

    @Test
    void generateAppendKeywordsExcel() throws IOException {
        // start to call modifyWorkbook
        DataFormatter formatter = new DataFormatter();
        Consumer<Row> rowsModifyConsumer = row -> {
            String contentCorrectWords = row.getCell(8).getStringCellValue();
            if (!"content_correct_words".equals(contentCorrectWords)) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(contentCorrectWords);
                    JSONArray checkResultList = jsonObject.getJSONArray("CheckResultList");
                    JSONArray seriousError = getErrorAndCorrectWordListByLevel(checkResultList, 3);
                    JSONArray seriousError2 = getErrorAndCorrectWordListByLevel(checkResultList, -3);
                    JSONArray error = getErrorAndCorrectWordListByLevel(checkResultList, 1);
                    seriousError = mergeJsonArray(seriousError, seriousError2);
                    Cell seriousErrorCell = row.createCell(9);
                    seriousErrorCell.setCellValue(getErrorAndCorrectWordString(seriousError));
                    Cell errorCell = row.createCell(10);
                    errorCell.setCellValue(getErrorAndCorrectWordString(error));
                } catch (JSONException e) {
                    log.info(contentCorrectWords);
                    throw new RuntimeException(e);
                }
            }
        };
        String outputFilePath = excelModifier.modifyRows("C:\\Users\\Taogen\\Desktop\\export\\审核-数据-1665479829882.xlsx", 0, rowsModifyConsumer);
        log.info(outputFilePath);
    }

    private String getErrorAndCorrectWordString(JSONArray seriousErrorJsonArray) {
        StringBuilder errorAndCorrectWord = new StringBuilder();
        for (int j = 0; j < seriousErrorJsonArray.length(); j++) {
            String errorWord = null;
            String corWord = null;
            try {
                errorWord = seriousErrorJsonArray.getJSONObject(j).getString("ErrWord");
                JSONArray corWordJsonArray = seriousErrorJsonArray.getJSONObject(j).getJSONArray("CorWord");
                corWord = corWordJsonArray.toList().stream().map(Object::toString).collect(Collectors.joining(","));
            } catch (JSONException e) {
                log.error(seriousErrorJsonArray.toString());
                log.error("获取错误词失败", e);
                continue;
            }
            errorAndCorrectWord.append(errorWord).append("（").append(corWord).append("）");
            if (j != seriousErrorJsonArray.length() - 1) {
                errorAndCorrectWord.append(" ");
            }
        }
        return errorAndCorrectWord.toString();
    }

    private JSONArray mergeJsonArray(JSONArray seriousErrorJsonArray, JSONArray leaderErrorJsonArray) throws JSONException {
        JSONArray resultJsonArray = new JSONArray();
        if (seriousErrorJsonArray != null && seriousErrorJsonArray.length() > 0) {
            for (int i = 0; i < seriousErrorJsonArray.length(); i++) {
                resultJsonArray.put(seriousErrorJsonArray.get(i));
            }
        }
        if (leaderErrorJsonArray != null && leaderErrorJsonArray.length() > 0) {
            for (int i = 0; i < leaderErrorJsonArray.length(); i++) {
                resultJsonArray.put(leaderErrorJsonArray.get(i));
            }
        }
        return resultJsonArray;
    }

    private JSONArray getErrorAndCorrectWordListByLevel(JSONArray checkResultJsonArray, int level) throws JSONException {
        JSONArray result = new JSONArray();
        for (int i = 0; i < checkResultJsonArray.length(); i++) {
            if (checkResultJsonArray.getJSONObject(i).getInt("level") == level) {
                result.put(checkResultJsonArray.getJSONObject(i));
            }
        }
        return result;
    }

    private String getKeywordPredicate(String keywords, String delimiter) {
        Function<String, String> function = item -> String.format("title like \"%%%s%%\" or content like \"%%%s%%\"", item, item);
//        Function<String, String> function = item -> String.format("(title like \"%%%s%%\" and title not like \"%%党的%s%%\") or (content like \"%%%s%%\" and content not like \"%%党的%s%%\")", item, item, item, item);
        return textModifier.splitModifyAndJoin(keywords, delimiter, function, " or ");
    }
}
