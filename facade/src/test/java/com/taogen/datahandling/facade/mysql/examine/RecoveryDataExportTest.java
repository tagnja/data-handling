package com.taogen.datahandling.facade.mysql.examine;

import com.taogen.commons.datatypes.string.StringUtils;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @ParameterizedTest
    @CsvSource({
            "1|2|3, AA|BB, CC, |"
    })
    @Disabled
    void exportFromAllTableToSingleExcel(String groupIds, String keywords, String excludeKeyword, String delimiter) throws IOException {
        long startTime = System.currentTimeMillis();
        if ("|".equals(delimiter)) {
            delimiter = "\\|"; // '|' need to add backslash '\\|'
        }
        List<Integer> groupIdList = Arrays.stream(groupIds.split(delimiter)).map(Integer::parseInt).collect(Collectors.toList());
        String newDelimiter = "---";
        keywords = textModifier.updateDelimiter(keywords, delimiter, newDelimiter);
        delimiter = newDelimiter;

        // 有 ID
        String sql = "select rd.id as \"ID\", rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\"\n" +
                "from app_wzjc.${table_name} rd \n" +
                "left join recovery_group rg on rg.id = rd.group_id \n" +
                "left join recovery_site rs on rs.id = rd.site_id\n" +
                "where group_id in (${group_id}) and (${keyword_predicate})";
//        String sql = "select rd.id as \"ID\", rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.gmt_create as \"入库时间\" ,rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\", content_correct_words\n" +
//                "from app_wzjc.${table_name} rd \n" +
//                "left join recovery_group rg on rg.id = rd.group_id \n" +
//                "left join recovery_site rs on rs.id = rd.site_id\n" +
//                "where group_id in (${group_id}) and (content_correct_words like '%\"level\":1%' or content_correct_words like '%\"level\":3%' or content_correct_words like '%\"level\":-3%')";
        String keywordPredicate = getKeywordPredicate(keywords, delimiter, excludeKeyword);
        sql = sql.replace("${keyword_predicate}", keywordPredicate);
        LabelAndData resultLabelAndData = new LabelAndData(new ArrayList<>(), new ArrayList<>());
        List<String> tableList = getTableList();
        for (Integer groupId : groupIdList) {
            Map<String, Object> groupMap = jdbcTemplate.queryForMap("select name from recovery_group where id = " + groupId);
            Object groupName = groupMap.get("name");
            String sqlAddGroup = sql.replace("${group_id}", groupId.toString());
            for (String table : tableList) {
                String queryTableSql = sqlAddGroup.replace("${table_name}", table);
                log.debug("queryTableSql is: {}", queryTableSql);
                SqlQueryParam sqlQueryParam = new SqlQueryParam();
                sqlQueryParam.setSql(queryTableSql);
                sqlQueryParam.setBatchFetch(true);
                LabelAndData tableLabelsAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
                resultLabelAndData.setLabels(tableLabelsAndData.getLabels());
                resultLabelAndData.getValuesList().addAll(tableLabelsAndData.getValuesList());
            }
        }
        String outputFileName = new StringBuilder()
                .append("审核-数据-")
                .append(System.currentTimeMillis())
                .append(".xlsx")
                .toString();
        String outputFilePath = excelWriter.writeLabelAndDataToExcel(resultLabelAndData,
                getExportDirPath() + File.separator + outputFileName);
        // 有 ID
        int appendToColumn = 7;
        int titleColumn = 1;
        int contentColumn = 2;
        String resultOutputFilePath = generateAppendKeywordsExcel(
                outputFilePath, keywords, delimiter,
                appendToColumn, titleColumn, contentColumn, true);
        log.info("output file path: {}", resultOutputFilePath);
        log.info("Elapsed time: {} ms", System.currentTimeMillis() - startTime);
    }

    @ParameterizedTest
    @CsvSource({
            "1|2|3, AA|BB|CC, CC, |"
    })
    void exportFromAllTableSeparateExcelByGroup(
            String groupIds, String keywords, String excludeKeyword, String delimiter) throws IOException {
        long startTime = System.currentTimeMillis();
        if ("|".equals(delimiter)) {
            delimiter = "\\|"; // '|' need to add backslash '\\|'
        }
        List<Integer> groupIdList = Arrays.stream(groupIds.split(delimiter)).map(Integer::parseInt).collect(Collectors.toList());
        String newDelimiter = "---";
        keywords = textModifier.updateDelimiter(keywords, delimiter, newDelimiter);
        delimiter = newDelimiter;

        // 有 ID
        String sql = "select rd.id as \"ID\", rd.title as \"标题\", rd.content as \"内容\", rd.pubtime as \"发布时间\", rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\"\n" +
                "from app_wzjc.${table_name} rd \n" +
                "left join recovery_group rg on rg.id = rd.group_id \n" +
                "left join recovery_site rs on rs.id = rd.site_id\n" +
                "where group_id in (${group_id}) and (${keyword_predicate})";
//        sql += " and rd.pubtime between '2023-01-01 00:00:00' and '2023-11-14 23:59:59'";
        String keywordPredicate = getKeywordPredicate(keywords, delimiter, excludeKeyword);
        sql = sql.replace("${keyword_predicate}", keywordPredicate);
        List<String> tableList = getTableList();
        for (Integer groupId : groupIdList) {
            Map<String, Object> groupMap = jdbcTemplate.queryForMap("select name from recovery_group where id = " + groupId);
            Object groupName = groupMap.get("name");
            String sqlAddGroup = sql.replace("${group_id}", groupId.toString());
            LabelAndData resultLabelAndData = new LabelAndData(new ArrayList<>(), new ArrayList<>());
            for (String table : tableList) {
                String queryTableSql = sqlAddGroup.replace("${table_name}", table);
                log.info("queryTableSql is: {}", queryTableSql);
                SqlQueryParam sqlQueryParam = new SqlQueryParam();
                sqlQueryParam.setSql(queryTableSql);
                sqlQueryParam.setBatchFetch(true);
                LabelAndData tableLabelsAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
                resultLabelAndData.setLabels(tableLabelsAndData.getLabels());
                resultLabelAndData.getValuesList().addAll(tableLabelsAndData.getValuesList());
            }
            // don't export empty data recovery group
            if (resultLabelAndData.getValuesList().isEmpty()) {
                continue;
            }
            String outputFileName = new StringBuilder()
                    .append("审核-数据-")
                    .append(Objects.toString(groupName))
                    .append(System.currentTimeMillis())
                    .append(".xlsx")
                    .toString();
            String outputFilePath = excelWriter.writeLabelAndDataToExcel(resultLabelAndData,
                    getExportDirPath() + File.separator + outputFileName);
            System.out.println("output file path: " + outputFilePath);
            // 有 ID 和 入库时间
            int appendToColumn = 7;
            int titleColumn = 1;
            int contentColumn = 2;

            // 是否标注关键词
//            keywords = addLowerOrUpperCaseKeywords(keywords, newDelimiter);
            String resultOutputFilePath = generateAppendKeywordsExcel(
                    outputFilePath, keywords, newDelimiter,
                    appendToColumn, titleColumn, contentColumn, true);
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

    private String addLowerOrUpperCaseKeywords(String keywords, String delimiter) {
        String[] keywordArray = keywords.split(delimiter);
        Set<String> keywordSet = new HashSet<>();
        Arrays.stream(keywordArray).forEach(item -> {
            keywordSet.add(item);
            keywordSet.add(item.toLowerCase());
            keywordSet.add(item.toUpperCase());
        });
        return String.join(delimiter, keywordSet);
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
        String outputFilePath = excelModifier.modifyRows(getExportDirPath() + "审核-数据-1665479829882.xlsx", 0, rowsModifyConsumer);
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

    private String generateAppendKeywordsExcel(String inputFilePath,
                                               String keywords,
                                               String delimiter,
                                               int appendToColumn,
                                               int titleColumn,
                                               int contentColumn,
                                               boolean ignoreCase) throws IOException {
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
                    .filter(item -> {
                        Pattern pattern = null;
                        if (ignoreCase) {
                            pattern = Pattern.compile(item, Pattern.CASE_INSENSITIVE);
                        } else {
                            pattern = Pattern.compile(item);
                        }
                        return pattern.matcher(title).find() || pattern.matcher(content).find();
                    })
                    .collect(Collectors.toList());
            log.trace("keywords: " + containsKeywords);
            Cell cell = row.createCell(appendToColumn);
            cell.setCellValue(String.join(delimiter, containsKeywords));
        };
        String outputFilePath = excelModifier.modifyRows(inputFilePath, 0, rowsModifyConsumer);
        return outputFilePath;
    }

    private String getKeywordPredicate(String keywords, String delimiter, String excludeKeyword) {
        Function<String, String> function = item -> String.format("title like \'%%%s%%\' or content like \'%%%s%%\'", item, item);
        if (StringUtils.isNotBlank(excludeKeyword)) {
            function = item -> String.format("(title like \"%%%s%%\" and title not like \"%%" + excludeKeyword + "%%\") or (content like \"%%%s%%\" and content not like \"%%" + excludeKeyword + "%%\")", item, item, item, item);
        }
        String result = textModifier.splitModifyAndJoin(keywords, delimiter, function, " or ");
//        result += String.format(" or (title like \"%%%s%%\" and title not like \"%%学习贯彻%s%%\") or (content like \"%%%s%%\" and content not like \"%%学习贯彻%s%%\")", "习近平新时代中国特色社会主义思想主题教育", "习近平新时代中国特色社会主义思想主题教育", "习近平新时代中国特色社会主义思想主题教育", "习近平新时代中国特色社会主义思想主题教育");
        return result;
    }


}
