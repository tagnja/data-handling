package com.taogen.datahandling.office.service;

import com.taogen.datahandling.common.vo.LabelAndData;

import java.io.IOException;

/**
 * @author taogen
 */
public interface ExcelWriter {
    String writeLabelAndDataToExcel(LabelAndData tableLabelAndData,
                                    String outputFilePath) throws IOException;
}
