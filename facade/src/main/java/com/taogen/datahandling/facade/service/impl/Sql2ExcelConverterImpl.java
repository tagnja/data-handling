package com.taogen.datahandling.facade.service.impl;

import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.service.Sql2ExcelConverter;
import com.taogen.datahandling.mysql.service.MySQLReader;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import com.taogen.datahandling.office.service.ExcelWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author taogen
 */
@Slf4j
@Component
public class Sql2ExcelConverterImpl implements Sql2ExcelConverter {
    @Autowired
    private MySQLReader mysqlReader;
    @Autowired
    private ExcelWriter excelWriter;

    @Override
    public void convert(JdbcTemplate jdbcTemplate, SqlQueryParam sqlQueryParam, String filePath) throws IOException {
        long startTime = System.currentTimeMillis();
        LabelAndData tableLabelAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
        String outputPath = excelWriter.writeLabelAndDataToExcel(tableLabelAndData, filePath);
        log.info("Elapsed time is {} ms", System.currentTimeMillis() - startTime);
        log.info("output file path: {}", outputPath);
    }
}
