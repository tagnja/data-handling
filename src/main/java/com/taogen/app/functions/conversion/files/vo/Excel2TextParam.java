package com.taogen.app.functions.conversion.files.vo;

import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Taogen
 */
public class Excel2TextParam {
    private Boolean deduplicate;
    private Predicate<Row> excelRowFilter;
    private Function<Row, List> itemConverter;
    private Boolean orderByAsc = true;

}
