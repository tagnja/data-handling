package com.taogen.datahandling.office.excel.service.service;

import com.taogen.datahandling.common.vo.LabelAndData;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

/**
 * @author taogen
 */
public interface WordWriter {
    String writeLabelAndDataToExcel(LabelAndData tableLabelAndData,
                                    String outputFilePath) throws IOException, InvalidFormatException;
}
