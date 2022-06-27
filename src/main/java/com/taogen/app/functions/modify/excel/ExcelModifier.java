package com.taogen.app.functions.modify.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Taogen
 */
public interface ExcelModifier {
    /**
     * @param inputFilePath
     * @param rowsModifyConsumer
     * @return Modified file path
     * @throws FileNotFoundException
     */
    String modifyRows(String inputFilePath, Consumer<Row> rowsModifyConsumer) throws IOException;

    /**
     * @param inputFilePath
     * @param workbookModifyConsumer
     * @return Modified file path
     * @throws FileNotFoundException
     */
    String modifyWorkbook(String inputFilePath, Consumer<XSSFWorkbook> workbookModifyConsumer) throws IOException;
}
