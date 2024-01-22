package com.taogen.datahandling.office.excel.service.service;

import com.taogen.commons.io.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
class ExcelMergerTest {

    @Autowired
    private ExcelMerger excelMerger;

    @Autowired
    private ExcelReader excelReader;

    @Test
    void mergeFiles() throws IOException, InvalidFormatException, URISyntaxException {
        String inputFilePath1 = FileUtils.getFilePathByFileClassPath("excel/excel1.xlsx");
        List<List<Object>> list1 = excelReader.readToList(Arrays.asList(inputFilePath1));
        assertEquals(3, list1.size());
        String inputFilePath2 = FileUtils.getFilePathByFileClassPath("excel/excel2.xlsx");
        List<List<Object>> list2 = excelReader.readToList(Arrays.asList(inputFilePath2));
        assertEquals(3, list2.size());
        List<String> inputFilePaths = Arrays.asList(inputFilePath1, inputFilePath2);
        String outputFilePath = excelMerger.mergeFiles(inputFilePaths, FileUtils.getFilePathByFileClassPath("excel"), true);
        log.debug(outputFilePath);
        assertNotNull(outputFilePath);
        List<List<Object>> lists = excelReader.readToList(Arrays.asList(outputFilePath));
        assertEquals(5, lists.size());
    }
}
