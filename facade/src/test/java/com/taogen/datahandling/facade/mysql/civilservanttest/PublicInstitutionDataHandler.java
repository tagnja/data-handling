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
public class PublicInstitutionDataHandler extends ExportBaseTest {

    @Test
    void jiangsuPublicInstitionExam() throws IOException, InvalidFormatException {
        log.debug("jiangsuPublicInstitionExam");
        String tableName = "jiangsu_public_institution_position";
        List<String> columns = mysqlReader.getTableColumns(jdbcTemplate, tableName);
        columns.remove(0);
        log.debug("columns: {}", columns);
        String inputFilePath = DirectoryUtils.getUserHomeDir() + "/Downloads/" + "南京市2024年事业单位统一公开招聘工作人员岗位信息表 (1).xls";
        log.debug("inputFilePath: {}", inputFilePath);
        ExcelReaderOption excelReaderOption = new ExcelReaderOption();
        excelReaderOption.setTotalSheetNum(1);
        excelReaderOption.setLabelRow(4);
        LabelAndData labelAndData = excelReader.read(Arrays.asList(inputFilePath), excelReaderOption);
        log.debug("labels: {}", labelAndData.getLabels());
        List<List<Object>> lists = labelAndData.getValuesList();
//        log.debug("lists: {}", lists.stream().map(Objects::toString).collect(Collectors.joining("\n")));
        mySQLWriter.write(jdbcTemplate, tableName, columns, lists);

    }
}
