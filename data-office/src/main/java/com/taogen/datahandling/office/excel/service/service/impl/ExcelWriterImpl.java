package com.taogen.datahandling.office.excel.service.service.impl;

import com.taogen.commons.office.poi.ExcelUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.office.excel.annotation.Excel;
import com.taogen.datahandling.office.excel.service.service.ExcelWriter;
import com.taogen.datahandling.office.excel.vo.FieldExcel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author taogen
 */
@Slf4j
@Component
public class ExcelWriterImpl implements ExcelWriter {

    public static final List<Class> DATE_TIME_CLASS_TYPES = Arrays.asList(Date.class, LocalTime.class);

    public static List<FieldExcel> getFieldExcelList(Class cls) {
        List<Field> fields = new ArrayList<>();
        if (!Object.class.equals(cls.getSuperclass())) {
            fields.addAll(Arrays.asList(cls.getSuperclass().getDeclaredFields()));
        }
        fields.addAll(Arrays.asList(cls.getDeclaredFields()));
        if (fields.isEmpty()) {
            return Collections.emptyList();
        }
        return fields.stream()
                .filter(item -> item.isAnnotationPresent(Excel.class))
                .map(item -> {
                    item.setAccessible(true);
                    return new FieldExcel(item, item.getAnnotation(Excel.class));
                })
                .sorted(Comparator.comparing(item -> item.getExcel().sortNum()))
                .collect(Collectors.toList());
    }

    private static void writeHeaderToSheet(XSSFWorkbook workbook, XSSFSheet sheet, List<FieldExcel> fieldExcelList) {
        int rowNum = 0;
        int columnNum = 0;
        Row row = sheet.createRow(rowNum);
        for (FieldExcel fieldExcel : fieldExcelList) {
            sheet.setColumnWidth(columnNum, (int) ((fieldExcel.getExcel().width() + 0.72) * 256));
            CellUtil.createCell(row, columnNum++, fieldExcel.getExcel().name(), getHeaderCellStyle(workbook, fieldExcel.getExcel()));
        }
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

    private static CellStyle getHeaderCellStyle(XSSFWorkbook workbook, Excel excel) {
        CellStyle labelStyle = workbook.createCellStyle();
        XSSFFont labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelFont.setFontHeight(excel.height());
        labelStyle.setFont(labelFont);
        return labelStyle;
    }

    private static void writeDataToSheet(XSSFWorkbook workbook, XSSFSheet sheet, List<FieldExcel> fieldExcelList, List list) {
        int rowNum = 1;
        int columnNum = 0;
        CellStyle contentStyle = getContentStyle(workbook);
        for (Object obj : list) {
            Row row = sheet.createRow(rowNum);
            columnNum = 0;
            for (FieldExcel fieldExcel : fieldExcelList) {
                Object fieldValue = null;
                try {
                    fieldValue = fieldExcel.getField().get(obj);
                    if (DATE_TIME_CLASS_TYPES.contains(fieldExcel.getField().getType()) && fieldExcel.getExcel().dateFormat() != null) {
                        fieldValue = formatDateTime(fieldValue, fieldExcel.getExcel().dateFormat());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                CellUtil.createCell(row, columnNum,
                        fieldValue != null ? fieldValue.toString() : "",
                        contentStyle);
                columnNum++;
            }
            rowNum++;
        }
    }

    private static Object formatDateTime(Object fieldValue, String dateFormat) {
        return new SimpleDateFormat(dateFormat).format(fieldValue);
    }

    private static CellStyle getContentStyle(XSSFWorkbook workbook) {
        CellStyle contentStyle = workbook.createCellStyle();
        XSSFFont rightFont = workbook.createFont();
        rightFont.setFontHeight(12);
        contentStyle.setFont(rightFont);
        return contentStyle;
    }

    @Override
    public String writeLabelAndDataToExcel(LabelAndData tableLabelAndData, String outputFilePath) throws IOException {
        long writeExcelStartTime = System.currentTimeMillis();
        List<String> labels = tableLabelAndData.getLabels();
        List<List<Object>> valuesList = tableLabelAndData.getValuesList();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            XSSFCellStyle titleCellStyle = createTitleCellStyle(sheet.getWorkbook());
            int rowNum = 0;
            writeTitle(sheet, rowNum, labels, titleCellStyle);
            rowNum++;
            writeData(sheet, rowNum, valuesList);
            writeExcelToFile(workbook, outputFilePath);
        }
        log.info("write to excel cost time: {} ms", System.currentTimeMillis() - writeExcelStartTime);
        return outputFilePath;
    }

    private void writeExcelToFile(XSSFWorkbook workbook, String outputFilePath) throws IOException {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(outputFilePath))) {
            workbook.write(outputStream);
        }
    }

    @Override
    public String writeObjectListOnlyExcelField(Class entityClass, List entityList, String outputFilePath) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            List<FieldExcel> fieldExcelList = getFieldExcelList(entityClass);
            writeHeaderToSheet(workbook, sheet, fieldExcelList);
            writeDataToSheet(workbook, sheet, fieldExcelList, entityList);
            writeExcelToFile(workbook, outputFilePath);
        }
        return outputFilePath;
    }

}
