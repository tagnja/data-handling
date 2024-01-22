package com.taogen.datahandling.facade.mysql.crm;

import com.taogen.commons.io.FileUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.Map;

/**
 * @author taogen
 */
@Slf4j
@Disabled
public class CustomerServcieLogExportTest extends ExportBaseTest {

    @BeforeEach
    void beforeEach() {
        showConfig();
    }

    @ParameterizedTest
    @CsvSource({
            "816,2021-01-01 00:00:00"
    })
    @Disabled
    public void exportServiceLog(Integer customerId, String startTime) throws IOException, InvalidFormatException {
        Map<String, Object> groupMap = jdbcTemplate.queryForMap("select name from crm.crm_customer where id = " + customerId);
        Object customerName = groupMap.get("name");

        String sql = "select a.gmt_create as '创建时间', b.name as '创建人', a.record as '内容'  " +
                "from crm.crm_customer_servcie_log as a left join crm.crm_user as b on a.user_id = b.id " +
                "where a.is_deleted = 0 and " +
                "a.customer_id = '" + customerId + "' and a.gmt_create >= '" + startTime + "' ";
        SqlQueryParam sqlQueryParam = new SqlQueryParam();
        sqlQueryParam.setSql(sql);
        LabelAndData result = mysqlReader.read(jdbcTemplate, sqlQueryParam);
        log.debug("result size: {}", result.getValuesList().size());
        String outputFilePath = getExportDirPath() + FileUtils.appendDateTimeToFileName(customerName + "_客户服务记录.docx");
        wordWriter.writeLabelAndDataToExcel(result, outputFilePath);
    }
}
