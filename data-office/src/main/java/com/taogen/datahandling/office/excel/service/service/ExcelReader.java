package com.taogen.datahandling.office.excel.service.service;

import com.taogen.datahandling.common.vo.LabelAndData;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.List;

/**
 * @author taogen
 */
public interface ExcelReader {
    /**
     * Read data from excel files
     * <p>
     * First row is lables, others are data.
     *
     * @param inputFilePaths
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    LabelAndData read(List<String> inputFilePaths) throws IOException, InvalidFormatException;

    List<List<Object>> readToList(List<String> inputFilePaths) throws IOException, InvalidFormatException;
}
