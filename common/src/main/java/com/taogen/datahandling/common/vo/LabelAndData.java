package com.taogen.datahandling.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author taogen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabelAndData {
    private List<String> labels;
    private List<List<Object>> valuesList;
}
