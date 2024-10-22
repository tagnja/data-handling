package com.taogen.datahandling.facade.mysql.civilservanttest;

import com.taogen.commons.io.DirectoryUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import com.taogen.datahandling.office.excel.vo.ExcelReaderOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author taogen
 */
@Slf4j
public class CivilServantDataHandler extends ExportBaseTest {

    @Test
    void test() throws IOException, InvalidFormatException {
        String inputFilePath = DirectoryUtils.getUserHomeDir() + "/Downloads/" + "2024年国家公务员考试职位表（3.96万人）.xls";
        log.debug("inputFilePath: {}", inputFilePath);
        ExcelReaderOption excelReaderOption = new ExcelReaderOption();
        excelReaderOption.setTotalSheetNum(4);
        excelReaderOption.setLabelRow(2);
        LabelAndData labelAndData = excelReader.read(Arrays.asList(inputFilePath), excelReaderOption);
        log.debug("labels: {}", labelAndData.getLabels());
        int columnNum = labelAndData.getLabels().size();
        List<List<Object>> lists = labelAndData.getValuesList();
//        log.debug("lists: {}", lists.stream().map(Objects::toString).collect(Collectors.joining("\n")));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("insert into civil_servant_position \n" +
                "(dept_code, dept_name, division, institutional_nature, position, position_attribute, region, position_desc, position_code, institutional_level, exam_category, number_of_positions, major, acdemic_qualifacation, acdemic_degree, political_profile, min_years_of_working, work_project_experience, professional_test_in_interview, interviewer_ratio, work_place, settlement_place, remark, dept_website, phone_number1, phone_number2, phone_number3) \n" +
                "values\n");
        for (List<Object> list : lists) {
            sqlBuilder.append("(");
            for (int i = 0; i < columnNum; i++) {
                if (i >= list.size()) {
                    sqlBuilder.append("null,");
                    continue;
                }
                sqlBuilder.append("'").append(list.get(i)).append("',");
            }
            sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
            sqlBuilder.append("),\n");
        }
        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
        log.debug("sql: {}", sqlBuilder.toString());
        jdbcTemplate.execute(sqlBuilder.toString());
    }
}
