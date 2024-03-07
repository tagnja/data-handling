package com.taogen.datahandling.facade.mysql.examine;

import com.taogen.commons.datatypes.string.StringUtils;
import com.taogen.commons.io.FileUtils;
import com.taogen.commons.regex.RegexUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author taogen
 */
@Slf4j
public class RecoveryDataPushErrorWordsTest extends ExportBaseTest {
    public static final Pattern PUSH_ERROR_WORDS_PATTERN = Pattern.compile(
            "(《|》|[(]|[)]|（|）|\\w|[\\u4E00-\\u9FA5])+(\\s*)(=+》)(\\s*)(《|》|[(]|[)]|（|）|\\w|[\\u4E00-\\u9FA5])+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    @Test
    void exportPushErrorWords() throws IOException {
        LabelAndData resultLabelAndData = new LabelAndData(new ArrayList<>(), new ArrayList<>());
        String sql = "select id,push_error_words\n" +
                "from app_wzjc.${table_name} as a\n" +
                "where push_status in (3, 6) and push_error_words like '%=》%'";
        List<String> tableList = getTableList();
        SqlQueryParam sqlQueryParam = new SqlQueryParam();
        sqlQueryParam.setBatchFetch(true);
        for (String table : tableList) {
            String queryTableSql = sql.replace("${table_name}", table);
            log.info("queryTableSql is: {}", queryTableSql);
            sqlQueryParam.setSql(queryTableSql);
            LabelAndData tableLabelsAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
            log.debug("tableLabelsAndData size is {}", tableLabelsAndData.getValuesList().size());
            resultLabelAndData.setLabels(tableLabelsAndData.getLabels());
            resultLabelAndData.getValuesList().addAll(tableLabelsAndData.getValuesList());
        }
        String outputFileName = new StringBuilder()
                .append("审核-数据-推送文章-错误词")
                .append(System.currentTimeMillis())
                .append(".xlsx")
                .toString();
        String outputFilePath = excelWriter.writeLabelAndDataToExcel(resultLabelAndData,
                getExportDirPath() + File.separator + outputFileName);
        System.out.println("output file path: " + outputFilePath);
    }

    @Test
    void deduplicatePushErrorWordsFromKeywords() throws IOException, InvalidFormatException {
        String inputFilePath = getExportDirPath() + FileUtils.appendDateTimeToFileName("审核-数据-推送文章-错误词.xlsx");
        LabelAndData labelAndData = excelReader.read(Arrays.asList(inputFilePath));
        List<List<Object>> valuesList = labelAndData.getValuesList();
        Set<String> errorWordSet = new LinkedHashSet<>();
        Set<String> recoveryKeywords = getRecoveryKeywords();
        for (List<Object> values : valuesList) {
            String pushErrorWords = Objects.toString(values.get(1));
            pushErrorWords = pushErrorWords.replace("\r", " ").replace("\n", " ").replace("\r\n", " ");
            Map<Integer, String> groupToReplacement = new HashMap<>();
            groupToReplacement.put(2, "");
            groupToReplacement.put(3, "|");
            groupToReplacement.put(4, "");
            pushErrorWords = RegexUtils.replaceMatchGroups(pushErrorWords, PUSH_ERROR_WORDS_PATTERN, groupToReplacement);
            Arrays.stream(pushErrorWords.split(" "))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .filter(item -> item.contains("|"))
                    .filter(item -> !item.substring(0, 1).equals("|"))
                    .filter(item -> !item.contains("???"))
                    .filter(item -> !recoveryKeywords.contains(item))
                    .forEach(errorWordSet::add);
        }
        for (String errorWord : errorWordSet) {
            System.out.println(errorWord);
        }
        // all: 4704
        // deduplicate: 4557
        System.out.println(errorWordSet.size());
        LabelAndData output = new LabelAndData();
        output.setLabels(Arrays.asList("错误词", "正确词"));
        output.setValuesList(errorWordSet.stream().map(item ->
                        Arrays.stream(item.split("\\|"))
                                .map(word -> (Object) word)
                                .collect(Collectors.toList()))
                .collect(Collectors.toList()));
        System.out.println(output.getValuesList().size());
        System.out.println(output.getValuesList());
        String outputFilePath = excelWriter.writeLabelAndDataToExcel(output, getExportDirPath() + File.separator + "审核-数据-推送文章-错误词-去重2.xlsx");
    }


    Set<String> getRecoveryKeywords() {
        String sql = "select content,name from recovery_keyword";
        SqlQueryParam sqlQueryParam = new SqlQueryParam();
        sqlQueryParam.setSql(sql);
        LabelAndData labelAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
        List<List<Object>> valuesList = labelAndData.getValuesList();
        Set<String> keywordSet = new LinkedHashSet<>();
        for (List<Object> values : valuesList) {
            String content = Objects.toString(values.get(0));
            String name = Objects.toString(values.get(1));
            keywordSet.add(content + "|" + name);
        }
        log.debug("recoveryKeyword size: " + keywordSet.size());
        return keywordSet;
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

}
