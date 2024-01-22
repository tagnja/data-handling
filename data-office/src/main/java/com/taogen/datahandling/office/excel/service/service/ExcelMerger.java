package com.taogen.datahandling.office.excel.service.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.List;

/**
 * @author taogen
 */
public interface ExcelMerger {
    String mergeFiles(List<String> inputFilePaths, String outputDir, boolean duplicate) throws IOException, InvalidFormatException;
}
