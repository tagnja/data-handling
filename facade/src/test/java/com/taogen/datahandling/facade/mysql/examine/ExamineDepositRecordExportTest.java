package com.taogen.datahandling.facade.mysql.examine;

import com.taogen.commons.io.FileUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Taogen
 */
@Slf4j
@Disabled
class ExamineDepositRecordExportTest extends ExportBaseTest {

    @BeforeEach
    void beforeEach() {
        showConfig();
    }

    @ParameterizedTest
    @CsvSource({
            "214, 2023-09-01 00:00:00, 2023-09-31 23:59:59",
            "267, 2023-09-01 00:00:00, 2023-09-31 23:59:59",
            "485, 2023-09-01 00:00:00, 2023-09-31 23:59:59"
    })
    void exportExamineDeposit(int deptId, String beginTime, String endTime) throws IOException {
        String sql = "select dept.dept_name as \"客户名称\", user.user_name \"账号名称\", count(*) \"消费次数\", SUM(LENGTH) as \"消费字数\"\n" +
                "from examine.examine_deposit_record as deposit \n" +
                "left join examine.sys_dept as dept on deposit.dept_id = dept.dept_id\n" +
                "left join examine.sys_user as user on deposit.create_by = user.user_id\n" +
                "WHERE\n" +
                "deposit.type in (1, 2, 5) and /*1在线审核，2文档审核，5智能改写*/\n" +
                "deposit.dept_id=? and \n" +
                "deposit.`length` < 0 and \n" +
                "deposit.create_time between ? and ?\n" +
                "group by deposit.create_by";
        String selectDeptNameSql = "select dept_name from examine.sys_dept where dept_id=?";
        Map<String, Object> deptNameMap = jdbcTemplate.queryForMap(selectDeptNameSql, deptId);
        String deptName = deptNameMap.get("dept_name").toString();
        SqlQueryParam sqlQueryParam = new SqlQueryParam();
        sqlQueryParam.setSql(sql);
        sqlQueryParam.setArgs(new Object[]{deptId, beginTime, endTime});
        LabelAndData labelAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
        String outputPath = getExportDirPath() + FileUtils.appendDateTimeToFileName(deptName + ".xlsx");
        outputPath = FileUtils.appendDateTimeToFileName(outputPath);
        log.debug("outputPath is: {}", outputPath);
        excelWriter.writeLabelAndDataToExcel(labelAndData, outputPath);
    }

    @ParameterizedTest
    @Disabled
    @CsvSource("2024-05-01 00:00:00, 2024-05-31 23:59:59")
    void exportExamineDepositTongzhou(String startTime, String endTime) throws IOException {
        String inputFilePath = getExportDirPath() + "南通通州教体局-accounts_2024-03-18_standard.xlsx";
        Integer userNameColNum = 1;
        Integer appendUseCountCol = 2;
        Integer appendUseCharNumCol = 3;
        DataFormatter formatter = new DataFormatter();
        Consumer<Row> rowsModifyConsumer = row -> {
            Cell userNameCell = row.getCell(userNameColNum);
            String userName = formatter.formatCellValue(userNameCell).trim();
            String query = "select user.user_name \"账号名称\", count(*) \"消费次数\", SUM(LENGTH) as \"消费字数\"\n" +
                    "from examine.examine_deposit_record as deposit \n" +
                    "right join examine.sys_user as user on deposit.create_by = user.user_id\n" +
                    "WHERE\n" +
                    "deposit.create_time between '" + startTime + "' and '" + endTime + "' and \n" +
                    "deposit.type in (1, 2, 5) and\n" +
                    "deposit.`length` < 0 and \n" +
                    "user.user_name = \"" + userName + "\"";
            Map<String, Object> result = jdbcTemplate.queryForMap(query);

            if (result.get("消费次数") != null) {
                row.createCell(appendUseCountCol).setCellValue(result.get("消费次数").toString());
            } else {
                row.createCell(appendUseCountCol).setCellValue(0);
            }
            if (result.get("消费字数") != null) {
                row.createCell(appendUseCharNumCol).setCellValue(result.get("消费字数").toString());
            } else {
                row.createCell(appendUseCharNumCol).setCellValue(0);
            }
        };
        String outputFile = excelModifier.modifyRows(inputFilePath, 1, rowsModifyConsumer);
        log.info("output file: {}", outputFile);
    }
}
