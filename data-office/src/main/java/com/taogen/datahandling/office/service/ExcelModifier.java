package com.taogen.datahandling.office.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
    String modifyRows(String inputFilePath,
                      int startRow,
                      Consumer<Row> rowsModifyConsumer) throws IOException;

    /**
     * Note: for large row files, removing rows is very slow. It's better to remove it before write to Excel.
     *
     * @param inputFilePath
     * @param rowsRemovePredicate
     * @return
     * @throws IOException
     */
    String removeRows(String inputFilePath, Predicate<Row> rowsRemovePredicate) throws IOException;

    /**
     * @param inputFilePath
     * @param workbookModifyConsumer
     * @return Modified file path
     * @throws FileNotFoundException
     */
    String modifyWorkbook(String inputFilePath, Consumer<XSSFWorkbook> workbookModifyConsumer) throws IOException;
}
