package com.taogen.datahandling.office.service.impl;

import com.taogen.commons.office.poi.ExcelUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.office.service.ExcelWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author taogen
 */
@Slf4j
@Component
public class ExcelWriterImpl implements ExcelWriter {

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
