package com.taogen.datahandling.office.excel.service.service.impl;

import com.taogen.commons.office.poi.ExcelUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.office.excel.service.service.ExcelReader;
import com.taogen.datahandling.office.excel.vo.ExcelReaderOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author taogen
 */
@Component
@Slf4j
public class ExcelReaderImpl implements ExcelReader {
    @Override
    public LabelAndData read(List<String> inputFilePaths) throws IOException, InvalidFormatException {
        LabelAndData labelAndData = new LabelAndData();
        DataFormatter formatter = new DataFormatter();
        List<List<Object>> data = new ArrayList<>();
        labelAndData.setValuesList(data);
        for (String filePath : inputFilePaths) {
            try (
                    FileInputStream inputStream = new FileInputStream(filePath);
                    Workbook workbook = ExcelUtils.createWorkbookByFileSuffix(inputStream, filePath.substring(filePath.lastIndexOf(".")));
            ) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> iterator = sheet.iterator();
                Row firstRow = iterator.next();
                labelAndData.setLabels(getCellValuesFromRow(firstRow, formatter).stream().map(Object::toString).collect(Collectors.toList()));
                Row row;
                while (iterator.hasNext()) {
                    row = iterator.next();
                    data.add(getCellValuesFromRow(row, formatter));
                }
            }
        }
        return labelAndData;
    }

    @Override
    public LabelAndData read(List<String> inputFilePaths, ExcelReaderOption options) throws IOException, InvalidFormatException {
        DataFormatter formatter = new DataFormatter();
        LabelAndData labelAndData = new LabelAndData();
        List<List<Object>> data = new ArrayList<>();
        labelAndData.setValuesList(data);
        for (String filePath : inputFilePaths) {
            try (
                    FileInputStream inputStream = new FileInputStream(filePath);
                    Workbook workbook = ExcelUtils.createWorkbookByFileSuffix(inputStream, filePath.substring(filePath.lastIndexOf(".")));
            ) {
                Integer sheetTotalNum = options.getTotalSheetNum();
                if (sheetTotalNum != null && sheetTotalNum > 0) {
                    int numberOfSheets = workbook.getNumberOfSheets();
                    if (sheetTotalNum > numberOfSheets) {
                        log.warn("Sheet number is greater than total sheet number. Total sheet number: {}, sheet number: {}", numberOfSheets, sheetTotalNum);
                        sheetTotalNum = numberOfSheets;
                    }
                    for (int i = 0; i < sheetTotalNum; i++) {
                        LabelAndData sheetLabelAndData = readSheet(workbook, i, options, formatter);
                        data.addAll(sheetLabelAndData.getValuesList());
                        labelAndData.setLabels(sheetLabelAndData.getLabels());
                    }
                } else {
                    LabelAndData sheetLabelAndData = readSheet(workbook, 0, options, formatter);
                    data.addAll(sheetLabelAndData.getValuesList());
                    labelAndData.setLabels(sheetLabelAndData.getLabels());
                }
            }
        }
        return labelAndData;
    }

    private LabelAndData readSheet(Workbook workbook, int sheetNum, ExcelReaderOption options, DataFormatter formatter) {
        LabelAndData labelAndData = new LabelAndData();
        List<List<Object>> data = new ArrayList<>();
        labelAndData.setValuesList(data);
        Sheet sheet = workbook.getSheetAt(sheetNum);
        Row row = null;
        Iterator<Row> iterator = sheet.iterator();
        if (options != null && options.getLabelRow() != null) {
            for (int i = 0; i < options.getLabelRow(); i++) {
                row = iterator.next();
            }
        }
        labelAndData.setLabels(getCellValuesFromRow(row, formatter).stream().map(Object::toString).collect(Collectors.toList()));
        while (iterator.hasNext()) {
            row = iterator.next();
            data.add(getCellValuesFromRow(row, formatter));
        }
        return labelAndData;
    }

    @Override
    public List<List<Object>> readToList(List<String> inputFilePaths) throws IOException, InvalidFormatException {
        DataFormatter formatter = new DataFormatter();
        List<List<Object>> data = new ArrayList<>();
        for (String filePath : inputFilePaths) {
            try (
                    FileInputStream inputStream = new FileInputStream(filePath);
                    Workbook workbook = ExcelUtils.createWorkbookByFileSuffix(inputStream, filePath.substring(filePath.lastIndexOf(".")));
            ) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> iterator = sheet.iterator();
                Row row;
                while (iterator.hasNext()) {
                    row = iterator.next();
                    data.add(getCellValuesFromRow(row, formatter));
                }
            }
        }
        return data;
    }

    private List<Object> getCellValuesFromRow(Row row, DataFormatter formatter) {
        List<Object> cellValues = new ArrayList<>();
        for (Cell cell : row) {
            cellValues.add(formatter.formatCellValue(cell));
        }
        return cellValues;
    }

}
