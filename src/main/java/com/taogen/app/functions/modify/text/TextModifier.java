package com.taogen.app.functions.modify.text;

import java.util.function.Function;

/**
 * @author Taogen
 */
public interface TextModifier {
    String splitModifyAndJoin(String source, String splitStr, Function<String, String> itemModifyFunc, String joinStr);
}
