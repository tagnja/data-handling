package com.taogen.datahandling.text.service;

import com.taogen.commons.io.FileUtils;
import com.taogen.commons.io.IOUtils;
import com.taogen.datahandling.text.service.impl.TextModifierImpl;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextModifierTest {

    private TextModifier textModifier = new TextModifierImpl();

    @Test
    void splitModifyAndJoin_string() {
        String s = "Tom、Jack、John";
        String delimiter = "、";
        Function<String, String> function = item -> String.format("title like \"%%%s%%\" or content like \"%%%s%%\"", item, item);
        String handledText = textModifier.splitModifyAndJoin(s, delimiter, function, " or ");
        // test
        String expect = "title like \"%Tom%\" or content like \"%Tom%\" or title like \"%Jack%\" or content like \"%Jack%\" or title like \"%John%\" or content like \"%John%\"";
        assertEquals(expect, handledText);
    }

    @Test
    void splitModifyAndJoinWithFile() throws IOException, URISyntaxException {
        String delimiter = "、";
        String intputFileClassPath = "text/splitModifyAndJoin.txt";
        String inputFilePath = FileUtils.getFilePathByFileClassPath(intputFileClassPath);
        Function<String, String> function = item -> String.format("title like \"%%%s%%\" or content like \"%%%s%%\"", item, item);
        String handledFilePath = textModifier.splitModifyAndJoinWithFile(inputFilePath,
                delimiter, function, " or ");
        String handledText = IOUtils.getTextFromFile(new File(handledFilePath));
        // test
        String expect = "title like \"%Tom%\" or content like \"%Tom%\" or title like \"%Jack%\" or content like \"%Jack%\" or title like \"%John%\" or content like \"%John%\" or title like \"%Jackson%\" or content like \"%Jackson%\"";
        assertEquals(expect, handledText);
    }

    @Test
    void updateDelimiter() {
        String source = "Jack,Tom,John";
        String delimiter = ",";
        String newDelimiter = "-";
        String result = textModifier.updateDelimiter(source, delimiter, newDelimiter);
        assertEquals("Jack-Tom-John", result);
    }
}
