package com.taogen.datahandling.es.vo;

import lombok.Data;

import java.util.List;

/**
 * @author taogen
 */
@Data
public class DslQueryParam {
    private List<String> index;
    private String dsl;
    private List<String> labels;
}
