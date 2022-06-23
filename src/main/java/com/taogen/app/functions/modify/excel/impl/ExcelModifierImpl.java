package com.taogen.app.functions.modify.excel.impl;

import com.taogen.app.functions.modify.excel.ExcelModifier;
import com.taogen.app.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Taogen
 */
@Component
@Slf4j
public class ExcelModifierImpl implements ExcelModifier {
    @Override
    public void modifyRows(String sourceDir, String sourceFileName,
                           Consumer<Row> rowsModifyConsumer) {
        StringBuilder inputFilePath = new StringBuilder()
                .append(sourceDir)
                .append(File.separator)
                .append(sourceFileName);
        StringBuilder outputFilePath = new StringBuilder()
                .append(sourceDir)
                .append(File.separator)
                .append(FileUtils.appendDateTimeToFileName(sourceFileName));
        try (
                XSSFWorkbook workbook = new XSSFWorkbook(new File(inputFilePath.toString()));
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            Row row;
            while (iterator.hasNext()) {
                row = iterator.next();
                rowsModifyConsumer.accept(row);
            }
            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    new FileOutputStream(outputFilePath.toString()))) {
                workbook.write(bufferedOutputStream);
                log.info("Modified file: {}", outputFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void modifyWorkbook(String sourceDir, String sourceFileName,
                               Consumer<XSSFWorkbook> workbookModifyConsumer) {
        StringBuilder inputFilePath = new StringBuilder()
                .append(sourceDir)
                .append(File.separator)
                .append(sourceFileName);
        StringBuilder outputFilePath = new StringBuilder()
                .append(sourceDir)
                .append(File.separator)
                .append(FileUtils.appendDateTimeToFileName(sourceFileName));
        try (
                XSSFWorkbook workbook = new XSSFWorkbook(new File(inputFilePath.toString()));
        ) {
            workbookModifyConsumer.accept(workbook);
            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    new FileOutputStream(outputFilePath.toString()))) {
                workbook.write(bufferedOutputStream);
                log.info("Modified file: {}", outputFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
    }
}
