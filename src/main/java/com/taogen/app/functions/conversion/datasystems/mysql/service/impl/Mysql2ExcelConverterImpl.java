package com.taogen.app.functions.conversion.datasystems.mysql.service.impl;

import com.taogen.app.functions.conversion.datasystems.mysql.service.Mysql2ExcelConverter;
import com.taogen.app.functions.conversion.datasystems.mysql.vo.SqlQueryParam;
import com.taogen.app.functions.conversion.datasystems.mysql.vo.TableLabelAndData;
import com.taogen.app.util.ExcelUtils;
import com.taogen.app.util.MysqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Taogen
 */
@Component
@Slf4j
public class Mysql2ExcelConverterImpl implements Mysql2ExcelConverter {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;


    @Override
    public String convertSql2ExcelV1(SqlQueryParam sqlQueryParam,
                                     String outputFileDir,
                                     String outputFileName) throws IOException {
        long startTime = System.currentTimeMillis();
        TableLabelAndData tableLabelAndData = getTableLabelsAndData(sqlQueryParam);
        String outputPath = writeTableLabelAndDataToExcel(tableLabelAndData, outputFileDir, outputFileName);
        log.info("Elapsed time is {} ms", System.currentTimeMillis() - startTime);
        log.info("output file path: {}", outputPath);
        return outputPath;
    }

    @Override
    public TableLabelAndData executeQuery(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            log.debug("connection: {}", connection.toString());
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    List<String> labels = getQueryLabelsByMetaData(metaData);
                    List<List<Object>> resultData = getQueryResultData(resultSet, metaData.getColumnCount());
                    return new TableLabelAndData(labels, resultData);
                }
            }
        }
    }

    @Override
    public TableLabelAndData getTableLabelsAndData(SqlQueryParam sqlQueryParam) {
        long fetchStartTime = System.currentTimeMillis();
        String sql = sqlQueryParam.getSql();
        log.info("sqlQueryParam sql is: {}", sql);
        Long count = getCount(sqlQueryParam);
        log.info("select count: {} row(s)", count);
        List<String> labels = getQueryLabels(sqlQueryParam);
        List<List<Object>> valuesList = new ArrayList<>();
        if (count > 0) {
            if (sqlQueryParam.getBatchFetch()) {
                valuesList = getValueListBatch(sqlQueryParam, count);
            } else {
                valuesList = getValueList(sqlQueryParam);
            }
        }
        log.info("values list size: {}", valuesList.size());
        if (count != valuesList.size()) {
            throw new RuntimeException("获取数据与实际的行数错误");
        }
        log.info("fetch data cost time: {} ms", System.currentTimeMillis() - fetchStartTime);
        return new TableLabelAndData(labels, valuesList);
    }

    private Long getCount(SqlQueryParam sqlQueryParam) {
        String sql = sqlQueryParam.getSql();
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        String selectCountSql = MysqlUtils.wrapperQueryToSelectCount(sql);
        log.info("select count sql: {}", selectCountSql);
        Map<String, Object> selectCountResult = jdbcTemplate.queryForMap(selectCountSql, args, argTypes);
        return Long.valueOf(selectCountResult.get("count").toString());
    }

    private List<List<Object>> getValueList(SqlQueryParam sqlQueryParam) {
        String sql = sqlQueryParam.getSql();
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, args, argTypes);
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        int columnNum = metaData.getColumnCount();
        return getQueryResultData(sqlRowSet, columnNum);
    }

    /**
     * set startId to fetch a page of data
     * Note: batch query sql must "order by id"
     *
     * @param sqlQueryParam
     * @param count
     * @return
     */
    private List<List<Object>> getValueListBatch(SqlQueryParam sqlQueryParam, Long count) {
        String sql = sqlQueryParam.getSql();
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        Integer batchSize = sqlQueryParam.getBatchSize();
        String selectFirstRowSql = MysqlUtils.wrapperQueryWithOrderByAndLimit(sql, sqlQueryParam.getPrimaryKeyColumn(), 1L);
        log.debug("select first row sql: {}", selectFirstRowSql);
        Map<String, Object> firstRow = jdbcTemplate.queryForMap(selectFirstRowSql, args, argTypes);
        log.debug("first row: {}", firstRow);
        Long firstRowId = Long.valueOf(firstRow.get(sqlQueryParam.getPrimaryKeyColumn()).toString());
        Long startId = firstRowId - 1;
        List<List<Object>> resultValueList = new ArrayList<>();
        int start = 0;
        while (start < count) {
            long size = batchSize > (count - start) ? (count - start) : batchSize;
            String primaryKeyPredicate = new StringBuilder()
                    .append(" ")
                    .append(sqlQueryParam.getPrimaryKeyColumn())
                    .append(" > ")
                    .append(startId)
                    .append(" order by ")
                    .append(sqlQueryParam.getPrimaryKeyColumn())
                    .toString();
            String batchSelectSql = MysqlUtils.wrapperPredicateToSql(sql, primaryKeyPredicate);
            batchSelectSql = MysqlUtils.wrapperQueryToSelectLimitSize(batchSelectSql, size);
            log.debug("batch select sql: {}", batchSelectSql);
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(batchSelectSql, args, argTypes);
            SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
            int columnNum = metaData.getColumnCount();
            List<List<Object>> queryResultData = getQueryResultData(sqlRowSet, columnNum);
            resultValueList.addAll(queryResultData);
            start += batchSize;
            startId = Long.valueOf(queryResultData.get(queryResultData.size() - 1).get(0).toString());
        }
        return resultValueList;
    }

    private List<String> getQueryLabels(SqlQueryParam sqlQueryParam) {
        String sql = sqlQueryParam.getSql();
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        String selectLabelSql = MysqlUtils.wrapperQueryToSelectLimitSize(sql, 0L);
        log.debug("select label sql: {}", selectLabelSql);
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(selectLabelSql, args, argTypes);
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        return getQueryLabelsByMetaData(metaData);
    }

    private List<List<Object>> getQueryResultData(SqlRowSet sqlRowSet, int columnNum) {
        List<List<Object>> valuesList = new ArrayList<>();
        while (sqlRowSet.next()) {
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < columnNum; i++) {
                Object value = sqlRowSet.getObject(i + 1);
                values.add(value);
            }
            valuesList.add(values);
        }
        return valuesList;
    }

    private List<List<Object>> getQueryResultData(ResultSet resultSet, int columnNum) throws SQLException {
        List<List<Object>> valuesList = new ArrayList<>();
        int row = 0;
        while (resultSet.next()) {
            row++;
            log.debug("row is {}", row);
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < columnNum; i++) {
                Object value = resultSet.getObject(i + 1);
                values.add(value);
            }
            valuesList.add(values);
        }
        return valuesList;
    }

    private List<String> getQueryLabelsByMetaData(ResultSetMetaData resultSetMetaData) throws SQLException {
        List<String> labels = new ArrayList<>();
        int columnNum = resultSetMetaData.getColumnCount();
        for (int i = 0; i < columnNum; i++) {
            labels.add(resultSetMetaData.getColumnLabel(i + 1));
        }
        return labels;
    }
    private List<String> getQueryLabelsByMetaData(SqlRowSetMetaData sqlRowSetMetaData) {
        List<String> labels = new ArrayList<>();
        int columnNum = sqlRowSetMetaData.getColumnCount();
        for (int i = 0; i < columnNum; i++) {
            labels.add(sqlRowSetMetaData.getColumnLabel(i + 1));
        }
        return labels;
    }

    @Override
    public String writeTableLabelAndDataToExcel(TableLabelAndData tableLabelAndData, String outputFileDir, String outputFileName) throws IOException {
        long writeExcelStartTime = System.currentTimeMillis();
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
        log.info("write to excel cost time: {} ms", System.currentTimeMillis() - writeExcelStartTime);
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
