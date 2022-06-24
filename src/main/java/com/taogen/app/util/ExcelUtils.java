package com.taogen.app.util;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

/**
 * @author Taogen
 */
public class ExcelUtils {
    public static boolean predicateExcel(String excelFilePath, Predicate<XSSFWorkbook> excelPredicate) {
        try (
                XSSFWorkbook workbook = new XSSFWorkbook(new File(excelFilePath));
        ) {
            return excelPredicate.test(workbook);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return false;
    }
}
