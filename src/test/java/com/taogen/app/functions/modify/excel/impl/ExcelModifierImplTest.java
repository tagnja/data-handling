package com.taogen.app.functions.modify.excel.impl;

import com.taogen.app.SpringBootBaseTest;
import com.taogen.app.functions.modify.excel.ExcelModifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
class ExcelModifierImplTest extends SpringBootBaseTest {

    @Autowired
    private ExcelModifier excelModifier;

    @Test
    void modifyRows_appendContainsSpecifiedWordToRows() throws FileNotFoundException {
        String s = "test1、test2";
        String[] names = s.split("、");
        DataFormatter formatter = new DataFormatter();
        Consumer<Row> rowsModifyConsumer = row -> {
            Cell titleCell = row.getCell(1);
            String title = formatter.formatCellValue(titleCell);
            System.out.println("title: " + title);
            Cell contentCell = row.getCell(2);
            String content = formatter.formatCellValue(contentCell);
            System.out.println("content: " + content);
            List<String> containsKeywords = Arrays.stream(names)
                    .filter(item -> title.contains(item) || content.contains(item))
                    .collect(Collectors.toList());
            System.out.println("keywords: " + containsKeywords);
            Cell cell = row.createCell(7);
            cell.setCellValue(String.join("、", containsKeywords));
        };
        excelModifier.modifyRows("C:/Users/Taogen/Desktop", "" +
                "test.xlsx", rowsModifyConsumer);
    }

    @Test
    void modifyWorkbook_appendContainsSpecifiedWordToRows() throws FileNotFoundException {
        String s = "test1、test2";
        String[] names = s.split("、");
        DataFormatter formatter = new DataFormatter();
        Consumer<XSSFWorkbook> workbookModifyConsumer = workbook -> {
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            Row row;
            while (iterator.hasNext()) {
                row = iterator.next();
                Cell titleCell = row.getCell(1);
                String title = formatter.formatCellValue(titleCell);
                System.out.println("title: " + title);
                Cell contentCell = row.getCell(2);
                String content = formatter.formatCellValue(contentCell);
                System.out.println("content: " + content);
                List<String> containsKeywords = Arrays.stream(names)
                        .filter(item -> title.contains(item) || content.contains(item))
                        .collect(Collectors.toList());
                System.out.println("keywords: " + containsKeywords);
                Cell cell = row.createCell(7);
                cell.setCellValue(String.join("、", containsKeywords));
            }
        };
        excelModifier.modifyWorkbook("C:/Users/Taogen/Desktop", "" +
                "test.xlsx", workbookModifyConsumer);
    }
}
