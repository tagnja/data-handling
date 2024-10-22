package com.taogen.datahandling.office.excel.vo;

import lombok.Data;

/**
 * @author taogen
 */
@Data
public class ExcelReaderOption {
    private Integer totalSheetNum;
    private Integer labelRow;
    private Integer dataStartRow;
    private Integer dataEndRow;
}
