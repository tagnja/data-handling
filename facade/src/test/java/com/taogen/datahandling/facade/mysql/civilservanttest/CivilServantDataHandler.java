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
class CivilServantDataHandler extends ExportBaseTest {

    @Test
    void test() throws IOException, InvalidFormatException {
        String inputFilePath = DirectoryUtils.getUserHomeDir() + "/Downloads/" + "中央机关及其直属机构2025年度考试录用公务员招考简章.xls";
        log.debug("inputFilePath: {}", inputFilePath);
        ExcelReaderOption excelReaderOption = new ExcelReaderOption();
        excelReaderOption.setTotalSheetNum(4);
        excelReaderOption.setLabelRow(2);
        LabelAndData labelAndData = excelReader.read(Arrays.asList(inputFilePath), excelReaderOption);
        log.debug("labels: {}", labelAndData.getLabels());
        List<List<Object>> lists = labelAndData.getValuesList();
//        log.debug("lists: {}", lists.stream().map(Objects::toString).collect(Collectors.joining("\n")));
        String columns = "dept_code, dept_name, division, institutional_nature, position, position_attribute, region, position_desc, position_code, institutional_level, exam_category, number_of_positions, major, academic_qualification, academic_degree, political_profile, min_years_of_working, work_project_experience, professional_test_in_interview, interviewer_ratio, work_place, settlement_place, remark, dept_website, phone_number1, phone_number2, phone_number3";
        mySQLWriter.write(jdbcTemplate, "civil_servant_position", Arrays.asList(columns.split(",")), lists);
    }
}
