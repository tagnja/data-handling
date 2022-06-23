package com.taogen.app.functions.modify.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.util.function.Consumer;

/**
 * @author Taogen
 */
public interface ExcelModifier {
    void modifyRows(String sourceDir, String sourceFileName, Consumer<Row> rowsModifyConsumer) throws FileNotFoundException;

    void modifyWorkbook(String sourceDir, String sourceFileName, Consumer<XSSFWorkbook> workbookModifyConsumer) throws FileNotFoundException;
}
