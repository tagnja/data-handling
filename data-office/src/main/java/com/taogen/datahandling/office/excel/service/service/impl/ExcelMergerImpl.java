package com.taogen.datahandling.office.excel.service.service.impl;

import com.taogen.commons.io.FileUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.office.excel.service.service.ExcelMerger;
import com.taogen.datahandling.office.excel.service.service.ExcelReader;
import com.taogen.datahandling.office.excel.service.service.ExcelWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author taogen
 */
@Slf4j
@Component
public class ExcelMergerImpl implements ExcelMerger {

    @Autowired
    private ExcelReader excelReader;

    @Autowired
    private ExcelWriter excelWriter;

    @Override
    public String mergeFiles(List<String> inputFilePaths, String outputDir, boolean duplicate) throws IOException, InvalidFormatException {
        LabelAndData labelAndData = excelReader.read(inputFilePaths);
        List<List<Object>> data = labelAndData.getValuesList();
        log.debug("data size is {}", data.size());
        if (duplicate) {
            LabelAndData.deduplicateData(data, 0, 8, "---");
            log.debug("data size after deduplicate is {}", data.size());
        }
        String outputFilePath = outputDir + File.separator + FileUtils.appendDateTimeToFileName("merge.xlsx");
        log.debug("output file path is {}", outputFilePath);
        return excelWriter.writeLabelAndDataToExcel(labelAndData, outputFilePath);
    }
}
