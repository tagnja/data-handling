package com.taogen.app.functions.modify.excel.impl;

import com.taogen.app.SpringBootBaseTest;
import com.taogen.app.functions.modify.excel.ExcelModifier;
import com.taogen.app.util.ExcelUtils;
import com.taogen.commons.io.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class ExcelModifierImplTest {

    private ExcelModifier excelModifier = new ExcelModifierImpl();

    @Test
    void modifyRows_appendContainsSpecifiedWordToRows() throws IOException, URISyntaxException {
        String keywords = "hello,world,developer";
        String delimiter = ",";
        String inputFileClassPath = "testfile/functions/excel/modifyRows_appendContainsSpecifiedWordToRows.xlsx";
        String inputFilePath = FileUtils.getFilePathByFileClassPath(inputFileClassPath);
        int appendToColumn = 3;
        // start to call modifyWorkbook
        String[] names = keywords.split(delimiter);
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
            Cell cell = row.createCell(appendToColumn);
            cell.setCellValue(String.join(delimiter, containsKeywords));
        };
        String outputFilePath = excelModifier.modifyRows(inputFilePath, rowsModifyConsumer);
        // test output file
        Predicate<XSSFWorkbook> excelPredicate = workbook -> {
            XSSFSheet sheet = workbook.getSheetAt(0);
            return Objects.equals("hello,developer", sheet.getRow(2).getCell(appendToColumn).getStringCellValue()) &&
                    Objects.equals("hello", sheet.getRow(4).getCell(appendToColumn).getStringCellValue()) &&
                    Objects.equals("hello,world", sheet.getRow(7).getCell(appendToColumn).getStringCellValue());
        };
        assertTrue(ExcelUtils.predicateExcel(outputFilePath, excelPredicate));
    }

    @Test
    void modifyWorkbook_appendContainsSpecifiedWordToRows() throws IOException, URISyntaxException {
        String keywords = "hello,world,developer";
        String delimiter = ",";
        String inputFileClassPath = "testfile/functions/excel/modifyRows_appendContainsSpecifiedWordToRows.xlsx";
        String inputFilePath = FileUtils.getFilePathByFileClassPath(inputFileClassPath);
        int appendToColumn = 3;
        // start to call modifyWorkbook
        String[] names = keywords.split(delimiter);
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
                Cell cell = row.createCell(appendToColumn);
                cell.setCellValue(String.join(delimiter, containsKeywords));
            }
        };
        String outputFilePath = excelModifier.modifyWorkbook(inputFilePath, workbookModifyConsumer);
        // test output file
        Predicate<XSSFWorkbook> excelPredicate = workbook -> {
            XSSFSheet sheet = workbook.getSheetAt(0);
            return Objects.equals("hello,developer", sheet.getRow(2).getCell(appendToColumn).getStringCellValue()) &&
                    Objects.equals("hello", sheet.getRow(4).getCell(appendToColumn).getStringCellValue()) &&
                    Objects.equals("hello,world", sheet.getRow(7).getCell(appendToColumn).getStringCellValue());
        };
        assertTrue(ExcelUtils.predicateExcel(outputFilePath, excelPredicate));
    }
}
