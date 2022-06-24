package com.taogen.app.functions.modify.text.impl;

import com.taogen.app.SpringBootBaseTest;
import com.taogen.app.functions.modify.text.TextModifier;
import com.taogen.commons.io.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class TextModifierImplTest extends SpringBootBaseTest {

    @Autowired
    private TextModifier textModifier;

    @Test
    void splitModifyAndJoin_string() {
        String s = "Tom、Jack、John";
        Function<String, String> function = item -> String.format("title like \"%%%s%%\" or content like \"%%%s%%\"", item, item);
        String handledText = textModifier.splitModifyAndJoin(s, "、", function, " or ");
        String expect = "title like \"%Tom%\" or content like \"%Tom%\" or title like \"%Jack%\" or content like \"%Jack%\" or title like \"%John%\" or content like \"%John%\"";
        assertEquals(expect, handledText);
    }

    @Test
    void splitModifyAndJoinWithFile() throws IOException, URISyntaxException {
        String inputFilePath = FileUtils.getFilePathByFileClassPath("testfile/functions/text/splitModifyAndJoin.txt");
        Function<String, String> function = item -> String.format("title like \"%%%s%%\" or content like \"%%%s%%\"", item, item);
        String handledFilePath = textModifier.splitModifyAndJoinWithFile(inputFilePath,
                "、", function, " or ");
        String handledText = FileUtils.getTextFromFile(handledFilePath);
        String expect = "title like \"%Tom%\" or content like \"%Tom%\" or title like \"%Jack%\" or content like \"%Jack%\" or title like \"%John%\" or content like \"%John%\" or title like \"%Jackson%\" or content like \"%Jackson%\"";
        assertEquals(expect, handledText);
    }
}
