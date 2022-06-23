package com.taogen.app.functions.modify.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.function.Consumer;

/**
 * @author Taogen
 */
public interface ExcelModifier {
    void modifyRows(String sourceDir, String sourceFileName, Consumer<Row> rowsModifyConsumer);

    void modifyWorkbook(String sourceDir, String sourceFileName, Consumer<XSSFWorkbook> workbookModifyConsumer);
}
