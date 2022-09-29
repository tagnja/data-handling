package com.taogen.app.functions.conversion.datasystems.service.impl;

import com.taogen.app.functions.conversion.datasystems.service.Mysql2ExcelConverter;
import com.taogen.app.functions.conversion.datasystems.vo.SqlQueryParam;
import com.taogen.app.functions.conversion.datasystems.vo.TableLabelAndData;
import com.taogen.app.util.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taogen
 */
@Component
@Slf4j
public class Mysql2ExcelConverterImpl implements Mysql2ExcelConverter {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String convertSql2ExcelV1(SqlQueryParam sqlQueryParam,
                                     String outputFileDir,
                                     String outputFileName) throws IOException {
        long fetchStartTime = System.currentTimeMillis();
        TableLabelAndData tableLabelAndData = getTableLabelsAndData(sqlQueryParam);
        log.info("fetch data cost time: {} ms", System.currentTimeMillis() - fetchStartTime);
        long writeExcelStartTime = System.currentTimeMillis();
        String outputPath = writeTableLabelAndDataToExcel(tableLabelAndData, outputFileDir, outputFileName);
        log.info("write to excel cost time: {} ms", System.currentTimeMillis() - writeExcelStartTime);
        log.info("output file path: {}", outputPath);
        return outputPath;
    }

    private TableLabelAndData getTableLabelsAndData(SqlQueryParam sqlQueryParam) {
        String sql = sqlQueryParam.getSql();
        log.info("sql is: {}", sql);
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        List<List<Object>> valuesList = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        jdbcTemplate.query(sql, args, argTypes, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int columnNum = rsMetaData.getColumnCount();
                if (labels.isEmpty()) {
                    for (int i = 0; i < columnNum; i++) {
                        labels.add(rsMetaData.getColumnLabel(i + 1));
                    }
                }
                List<Object> values = new ArrayList<>();
                for (int i = 0; i < columnNum; i++) {
                    Object value = rs.getObject(i + 1);
                    values.add(value);
                }
                valuesList.add(values);
            }
        });
        return new TableLabelAndData(labels, valuesList);
    }

    private String writeTableLabelAndDataToExcel(TableLabelAndData tableLabelAndData, String outputFileDir, String outputFileName) throws IOException {
        List<String> labels = tableLabelAndData.getLabels();
        List<List<Object>> valuesList = tableLabelAndData.getValuesList();
        String outputFilePath = new StringBuilder()
                .append(outputFileDir)
                .append(File.separator)
                .append(outputFileName)
                .toString();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            XSSFCellStyle titleCellStyle = createTitleCellStyle(sheet.getWorkbook());
            int rowNum = 0;
            writeTitle(sheet, rowNum, labels, titleCellStyle);
            rowNum++;
            writeData(sheet, rowNum, valuesList);
            try (BufferedOutputStream outputStream = new BufferedOutputStream(
                    new FileOutputStream(outputFilePath))) {
                workbook.write(outputStream);
            }
        }
        return outputFilePath;
    }

    private void writeData(XSSFSheet sheet, int rowNum, List<List<Object>> valuesList) {
        for (int i = 0; i < valuesList.size(); i++) {
            XSSFRow row = sheet.createRow(rowNum++);
            List<Object> values = valuesList.get(i);
            for (int colNum = 0; colNum < values.size(); colNum++) {
                XSSFCell cell = row.createCell(colNum);
                ExcelUtils.setCellValueByObject(cell, values.get(colNum));
            }
        }
    }

    private void writeTitle(XSSFSheet sheet,
                            int rowNum,
                            List<String> labels,
                            XSSFCellStyle titleCellStyle) {
        XSSFRow row = sheet.createRow(rowNum);
        int colNum = 0;
        for (; colNum < labels.size(); colNum++) {
            XSSFCell cell = row.createCell(colNum);
            cell.setCellStyle(titleCellStyle);
            ExcelUtils.setCellValueByObject(cell, labels.get(colNum));
        }
    }

    private XSSFCellStyle createTitleCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle titleCellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        titleCellStyle.setFont(font);
        byte[] rgb = {(byte) 149, (byte) 179, (byte) 215};
        titleCellStyle.setFillForegroundColor(new XSSFColor(rgb, new DefaultIndexedColorMap()));
        titleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return titleCellStyle;
    }

}
