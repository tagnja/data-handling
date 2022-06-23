package com.taogen.app.functions.modify.text.impl;

import com.taogen.app.SpringBootBaseTest;
import com.taogen.app.functions.modify.text.TextModifier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class TextModifierImplTest extends SpringBootBaseTest {

    @Autowired
    private TextModifier textModifier;

    @Test
    void splitModifyAndJoin() {
        String s = "Tom、Jack、John";
        Function<String, String> function = item -> String.format("title like \"%%%s%%\" or content like \"%%%s%%\"", item, item);
        textModifier.splitModifyAndJoin(s, "、", function, " or ");
    }
}
