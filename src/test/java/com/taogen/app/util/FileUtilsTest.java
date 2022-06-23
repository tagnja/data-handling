package com.taogen.app.util;

import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void appendDateTimeToFileName() {
        String fileName1 = "test.txt";
        String updateFileName1 = FileUtils.appendDateTimeToFileName(fileName1);
        System.out.println(updateFileName1);
        assertTrue(Pattern.compile("test_[0-9_-]+.txt").matcher(updateFileName1).matches());
        String fileName2 = "test";
        String updateFileName2 = FileUtils.appendDateTimeToFileName(fileName2);
        System.out.println(updateFileName2);
        assertTrue(Pattern.compile("test_[0-9_-]+").matcher(updateFileName2).matches());
    }

    @Test
    void ensureFileExists() throws FileNotFoundException {
        String tempDir = FileUtils.getTempDir();
        assertDoesNotThrow(() -> FileUtils.ensureFileExists(tempDir));
        String testFile = new StringBuilder()
                .append(tempDir)
                .append(File.separator)
                .append("test")
                .append(System.currentTimeMillis())
                .append(".txt")
                .toString();
        assertThrows(FileNotFoundException.class, () -> FileUtils.ensureFileExists(testFile));
    }
}
