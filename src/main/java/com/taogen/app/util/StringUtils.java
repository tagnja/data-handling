package com.taogen.app.util;

/**
 * @author Taogen
 */
public class StringUtils {
    public static String removeNewLineCharacters(String source) {
        if (source == null) {
            return null;
        }
        return source.replace("\r\n", "")
                .replace("\r", "")
                .replace("\n", "");
    }
}
