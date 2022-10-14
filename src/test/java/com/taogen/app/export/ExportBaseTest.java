package com.taogen.app.export;

import com.taogen.app.functions.conversion.datasystems.mysql.service.Mysql2ExcelConverter;
import com.taogen.app.functions.modify.excel.ExcelModifier;
import com.taogen.app.functions.modify.text.TextModifier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Taogen
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Disabled
public class ExportBaseTest {
    @Value("${spring.config.import}")
    protected String configImportFile;

    @Value("${spring.datasource.url}")
    protected String jdbcUrl;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected ExcelModifier excelModifier;

    @Autowired
    protected TextModifier textModifier;

    @Autowired
    protected Mysql2ExcelConverter mysql2ExcelConverter;

    protected void showConfig() {
        log.info("show config <<<");
        log.info("config import: {}", configImportFile);
        log.info("dataSource jdbcUrl: {}", jdbcUrl);
        log.info(">>>");
    }
}
