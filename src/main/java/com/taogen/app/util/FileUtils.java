package com.taogen.app.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Taogen
 */
public class FileUtils {
    private static final DateFormat YYYY_MM_SS_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    /**
     * For example: text.txt -> text_2022-06-23_16-01-01.txt, text -> text_2022-06-23_16-01-01
     * @param fileName
     * @return
     */
    public static String appendDateTimeToFileName(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return new StringBuilder()
                    .append(fileName, 0, index)
                    .append("_")
                    .append(YYYY_MM_SS_HH_MM_SS.format(new Date()))
                    .append(fileName.substring(index))
                    .toString();
        } else {
            return new StringBuilder()
                    .append(fileName)
                    .append("_")
                    .append(YYYY_MM_SS_HH_MM_SS.format(new Date()))
                    .toString();
        }
    }
}
