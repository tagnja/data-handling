package com.taogen.app.functions.conversion.datasystems.vo;

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
}
