package com.taogen.datahandling.office.excel.service.service.impl;

import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.office.excel.service.service.ExcelReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
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
            try (Workbook workbook = new XSSFWorkbook(new File(filePath))) {
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
    public List<List<Object>> readToList(List<String> inputFilePaths) throws IOException, InvalidFormatException {
        DataFormatter formatter = new DataFormatter();
        List<List<Object>> data = new ArrayList<>();
        for (String filePath : inputFilePaths) {
            try (Workbook workbook = new XSSFWorkbook(new File(filePath))) {
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
