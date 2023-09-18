package com.taogen.datahandling.text.service;

import java.io.FileNotFoundException;
import java.util.function.Function;

/**
 * @author Taogen
 */
public interface TextModifier {
    /**
     * @param source
     * @param splitDelimiter special delimiters need escape like "\\|"
     * @param itemModifyFunc
     * @param joinDelimiter
     * @return Modified string
     */
    String splitModifyAndJoin(String source, String splitDelimiter,
                              Function<String, String> itemModifyFunc,
                              String joinDelimiter);

    /**
     * @param inputFilePath
     * @param splitDelimiter special delimiters need escape like "\\|"
     * @param itemModifyFunc
     * @param joinDelimiter
     * @return Modified file path
     * @throws FileNotFoundException
     */
    String splitModifyAndJoinWithFile(String inputFilePath,
                              String splitDelimiter, Function<String, String> itemModifyFunc,
                              String joinDelimiter) throws FileNotFoundException;

    String updateDelimiter(String keywords, String delimiter, String newDelimiter);
}
