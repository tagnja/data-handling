package com.taogen.datahandling.facade.mysql.examine;

import com.taogen.commons.io.DirectoryUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author taogen
 */
@Slf4j
public class RecoveryKeywordTest extends ExportBaseTest {
    @Test
    void insertIntoRecoveryKeyword() throws IOException, InvalidFormatException {
//        String inputFilePath = getExportDirPath() + File.separator + "审核-数据-推送文章-错误词-去重2.xlsx";
        String inputFilePath = DirectoryUtils.getUserHomeDir() +
                File.separator + "Downloads" + File.separator + "审核-数据-推送文章-错误词-去重2.xlsx";
        LabelAndData labelAndData = excelReader.read(Arrays.asList(inputFilePath));
        List<List<Object>> valuesList = labelAndData.getValuesList();
        Set<String> recoveryKeywords = getRecoveryKeywords();
        log.debug("valueList size: {}", valuesList.size()); // 806
        StringBuilder sql = new StringBuilder()
                .append("insert into recovery_keyword (content, name, remark) values ");
        int i = 0;
        for (List<Object> values : valuesList) {
            String content = Objects.toString(values.get(0));
            String name = Objects.toString(values.get(1));
            if (!recoveryKeywords.contains(content + "|" + name)) {
                sql.append(String.format("(\"%s\", \"%s\", \"已推送关键词\"),", content, name));
                i++;
            }
        }
        log.debug("i: {}", i);
        sql.deleteCharAt(sql.length() - 1);
        System.out.println(sql.toString());
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

}
