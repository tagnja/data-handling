package com.taogen.datahandling.facade.base;

import com.taogen.datahandling.facade.service.Sql2ExcelConverter;
import com.taogen.datahandling.mysql.service.MySQLReader;
import com.taogen.datahandling.office.service.ExcelModifier;
import com.taogen.datahandling.office.service.ExcelWriter;
import com.taogen.datahandling.text.service.TextModifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
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
//    @Value("${spring.config.import}")
//    protected String configImportFile;

    @Value("${spring.datasource.url}")
    protected String jdbcUrl;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired(required = true)
    protected ExcelModifier excelModifier;

    @Autowired(required = true)
    protected TextModifier textModifier;

    @Autowired(required = true)
    protected Sql2ExcelConverter mysql2ExcelConverter;

    @Autowired(required = true)
    protected MySQLReader mysqlReader;

    @Autowired(required = true)
    protected ExcelWriter excelWriter;

    protected void showConfig() {
        log.info("show config <<<");
//        log.info("config import: {}", configImportFile);
        log.info("dataSource jdbcUrl: {}", jdbcUrl);
        log.info(">>>");
    }

    protected XSSFCellStyle createTitleCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle titleCellStyle = workbook.createCellStyle();
        XSSFFont xssfFont = workbook.createFont();
        xssfFont.setColor(IndexedColors.BLUE.getIndex());
        xssfFont.setBold(true);
        titleCellStyle.setFont(xssfFont);
        titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // backgroundColor: 221, 235, 247
        // borderColor: 155, 194, 230
//        byte[] rgb = {(byte) 149, (byte) 179, (byte) 215};
        byte[] backgroundRgb = {(byte) 221, (byte) 235, (byte) 247};
        byte[] borderRgb = {(byte) 155, (byte) 194, (byte) 230};
        XSSFColor borderXssfColor = new XSSFColor(borderRgb, new DefaultIndexedColorMap());
        titleCellStyle.setFillForegroundColor(new XSSFColor(backgroundRgb, new DefaultIndexedColorMap()));
        titleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleCellStyle.setBorderTop(BorderStyle.THICK);
        titleCellStyle.setTopBorderColor(borderXssfColor);
        titleCellStyle.setBorderBottom(BorderStyle.THICK);
        titleCellStyle.setBottomBorderColor(borderXssfColor);
        titleCellStyle.setBorderLeft(BorderStyle.THICK);
        titleCellStyle.setLeftBorderColor(borderXssfColor);
        titleCellStyle.setBorderRight(BorderStyle.THICK);
        titleCellStyle.setRightBorderColor(borderXssfColor);
        return titleCellStyle;
    }
}
