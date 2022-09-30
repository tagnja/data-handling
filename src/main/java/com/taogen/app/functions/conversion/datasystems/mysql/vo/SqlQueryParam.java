package com.taogen.app.functions.conversion.datasystems.mysql.vo;

import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class SqlQueryParam {
    private String sql;
    private Object[] args;
    /**
     * java.sql.Types
     */
    private int[] argTypes;

    private String primaryKeyColumn = "id";

    private Boolean batchFetch = false;

    private Integer batchSize = 50;

    /**
     * TODO
     */
    private Boolean currentFetch = false;
}
