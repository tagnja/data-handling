package com.taogen.app.functions.modify.text.impl;

import com.taogen.app.functions.modify.text.TextModifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Taogen
 */
@Slf4j
@Component
public class TextModifierImpl implements TextModifier {
    @Override
    public String splitModifyAndJoin(String source, String splitStr, Function<String, String> itemModifyFunc, String joinStr) {
        String[] splitStrArray = source.split(splitStr);
        log.debug("split array: {}", Arrays.toString(splitStrArray));
        String result = Arrays.stream(splitStrArray)
                .map(itemModifyFunc)
                .collect(Collectors.joining(joinStr));
        log.info("result is: {}", result);
        return result;
    }
}
