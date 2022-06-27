package com.taogen.app.functions.modify.excel.impl;

import com.taogen.app.functions.modify.excel.ExcelModifier;
import com.taogen.commons.io.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Taogen
 */
@Component
@Slf4j
public class ExcelModifierImpl implements ExcelModifier {
    @Override
    public String modifyRows(String inputFilePath,
                             Consumer<Row> rowsModifyConsumer) throws IOException {
        FileUtils.ensureFileExists(inputFilePath);
        String sourceDir = FileUtils.getDirPathByFilePath(inputFilePath);
        String sourceFileName = FileUtils.getFileNameByFilePath(inputFilePath);
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
        while (iterator.hasNext()) {
            row = iterator.next();
            rowsModifyConsumer.accept(row);
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
        FileUtils.ensureFileExists(inputFilePath);
        String sourceDir = FileUtils.getDirPathByFilePath(inputFilePath);
        String sourceFileName = FileUtils.getFileNameByFilePath(inputFilePath);
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
