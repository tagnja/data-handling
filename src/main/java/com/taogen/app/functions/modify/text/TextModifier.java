package com.taogen.app.functions.modify.text;

import java.io.FileNotFoundException;
import java.util.function.Function;

/**
 * @author Taogen
 */
public interface TextModifier {
    /**
     * @param source
     * @param splitStr
     * @param itemModifyFunc
     * @param joinStr
     * @return Modified string
     */
    String splitModifyAndJoin(String source, String splitStr,
                              Function<String, String> itemModifyFunc,
                              String joinStr);

    /**
     * @param inputFilePath
     * @param splitStr
     * @param itemModifyFunc
     * @param joinStr
     * @return Modified file path
     * @throws FileNotFoundException
     */
    String splitModifyAndJoinWithFile(String inputFilePath,
                              String splitStr, Function<String, String> itemModifyFunc,
                              String joinStr) throws FileNotFoundException;

}
