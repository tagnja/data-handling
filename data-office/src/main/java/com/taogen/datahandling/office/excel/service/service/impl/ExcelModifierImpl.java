package com.taogen.datahandling.office.excel.service.service.impl;

import com.taogen.commons.io.DirectoryUtils;
import com.taogen.commons.io.FileUtils;
import com.taogen.commons.office.poi.ExcelUtils;
import com.taogen.datahandling.office.excel.service.service.ExcelModifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Taogen
 */
@Component
@Slf4j
public class ExcelModifierImpl implements ExcelModifier {
    @Override
    public String modifyRows(String inputFilePath,
                             int startRowNum,
                             Consumer<Row> rowsModifyConsumer) throws IOException {
        DirectoryUtils.ensureFileDirExist(inputFilePath);
        String sourceDir = DirectoryUtils.getDirPathByFile(new File(inputFilePath));
        String sourceFileName = FileUtils.extractFileNameFromFilePath(inputFilePath);
        StringBuilder outputFilePath = new StringBuilder()
                .append(sourceDir)
                .append(File.separator)
                .append(FileUtils.appendDateTimeToFileName(sourceFileName));
        // it will update original Excel file, so need to copy and modify the copied file.
        Files.copy(Paths.get(inputFilePath), Paths.get(outputFilePath.toString()));
        // start to modify excel
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(outputFilePath.toString()));
        Workbook workbook = ExcelUtils.createWorkbookByFileSuffix(inputStream, "." + FileUtils.getFileExtension(inputFilePath));
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        Row row;
        int rowNum = 0;
        while (iterator.hasNext()) {
            row = iterator.next();
            if (rowNum >= startRowNum) {
                rowsModifyConsumer.accept(row);
            }
            rowNum++;
        }
        inputStream.close();
        BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(outputFilePath.toString()));
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        log.info("output file path: {}", outputFilePath);
        return outputFilePath.toString();
    }

    @Override
    public String removeRows(String inputFilePath, Predicate<Row> rowsRemovePredicate) throws IOException {
        DirectoryUtils.ensureFileDirExist(inputFilePath);
        String sourceDir = DirectoryUtils.getDirPathByFile(new File(inputFilePath));
        String sourceFileName = FileUtils.extractFileNameFromFilePath(inputFilePath);
        StringBuilder outputFilePath = new StringBuilder()
                .append(sourceDir)
                .append(File.separator)
                .append(FileUtils.appendDateTimeToFileName(sourceFileName));
        // it will update original Excel file, so need to copy and modify the copied file.
        Files.copy(Paths.get(inputFilePath), Paths.get(outputFilePath.toString()));
        // start to modify excel
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(outputFilePath.toString()));
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        Row row;
        List<Row> removingRows = new ArrayList<>();
        while (iterator.hasNext()) {
            row = iterator.next();
            if (rowsRemovePredicate.test(row)) {
                removingRows.add(row);
            }
        }
        log.debug("remove row num: {}", removingRows.size());
        for (Row removingRow : removingRows) {
            ExcelUtils.removeRow(sheet, removingRow.getRowNum());
        }
        inputStream.close();
        BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(outputFilePath.toString()));
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        log.info("output file path: {}", outputFilePath);
        return outputFilePath.toString();
    }

    @Override
    public String modifyWorkbook(String inputFilePath,
                                 Consumer<XSSFWorkbook> workbookModifyConsumer) throws IOException {
        DirectoryUtils.ensureFileDirExist(inputFilePath);
        String sourceDir = DirectoryUtils.getDirPathByFile(new File(inputFilePath));
        String sourceFileName = FileUtils.extractFileNameFromFilePath(inputFilePath);
        StringBuilder outputFilePath = new StringBuilder()
                .append(sourceDir)
                .append(File.separator)
                .append(FileUtils.appendDateTimeToFileName(sourceFileName));
        // it will update original Excel file, so need to copy and modify the copied file.
        Files.copy(Paths.get(inputFilePath), Paths.get(outputFilePath.toString()));
        // start to modify excel
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(outputFilePath.toString()));
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        workbookModifyConsumer.accept(workbook);
        inputStream.close();
        BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(outputFilePath.toString()));
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        log.info("output file path: {}", outputFilePath);
        return outputFilePath.toString();
    }
}
