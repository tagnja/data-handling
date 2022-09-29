package com.taogen.app.util;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Predicate;

/**
 * @author Taogen
 */
public class ExcelUtils {
    public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    public static void setCellValueByObject(XSSFCell cell, Object object) {
        if (cell == null) {
            return;
        }
        if (object == null) {
            cell.setCellValue("");
            return;
        }
        if (object instanceof String) {
            cell.setCellValue((String) object);
        } else if (object instanceof Byte) {
            cell.setCellValue((Byte) object);
        } else if (object instanceof Short) {
            cell.setCellValue((Short) object);
        } else if (object instanceof Integer) {
            cell.setCellValue((Integer) object);
        } else if (object instanceof Long) {
            cell.setCellValue((Long) object);
        } else if (object instanceof Float) {
            cell.setCellValue((Float) object);
        } else if (object instanceof Double) {
            cell.setCellValue((Double) object);
        } else if (object instanceof Boolean) {
            cell.setCellValue((Boolean) object);
        } else if (object instanceof Date) {
            cell.setCellValue(DATE_TIME_FORMAT.format(object));
        } else if (object instanceof LocalDate) {
            LocalDate localDate = (LocalDate) object;
            cell.setCellValue(localDate.format(DATE_FORMATTER));
        } else if (object instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) object;
            cell.setCellValue(localDateTime.format(DATE_TIME_FORMATTER));
        } else if (object instanceof Calendar) {
            Calendar calendar = (Calendar) object;
            cell.setCellValue(DATE_TIME_FORMAT.format(calendar.getTime()));
        } else if (object instanceof RichTextString) {
            cell.setCellValue((RichTextString) object);
        } else {
            cell.setCellValue(object.toString());
        }
    }

    public static short getDateTimeFormat(Workbook workbook, String format) {
        CreationHelper createHelper = workbook.getCreationHelper();
        return createHelper.createDataFormat().getFormat(format);
    }
}
