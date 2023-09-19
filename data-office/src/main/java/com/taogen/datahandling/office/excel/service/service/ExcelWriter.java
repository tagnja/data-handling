package com.taogen.datahandling.office.excel.service.service;

import com.taogen.datahandling.common.vo.LabelAndData;

import java.io.IOException;
import java.util.List;

/**
 * @author taogen
 */
public interface ExcelWriter {
    String writeLabelAndDataToExcel(LabelAndData tableLabelAndData,
                                    String outputFilePath) throws IOException;

    String writeObjectListOnlyExcelField(Class entityClass, List entityList,
                                         String outputFilePath) throws IOException;
}
