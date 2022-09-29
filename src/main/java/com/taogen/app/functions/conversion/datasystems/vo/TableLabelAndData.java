package com.taogen.app.functions.conversion.datasystems.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author Taogen
 */
@Data
@AllArgsConstructor
public class TableLabelAndData {
    private List<String> labels;
    private List<List<Object>> valuesList;
}
