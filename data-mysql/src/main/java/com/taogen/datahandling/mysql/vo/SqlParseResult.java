package com.taogen.datahandling.mysql.vo;

import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class SqlParseResult {

    private String select;
    private String from;
    private String join;
    private String where;
    private String groupBy;
    private String orderBy;
    private String limit;
}
