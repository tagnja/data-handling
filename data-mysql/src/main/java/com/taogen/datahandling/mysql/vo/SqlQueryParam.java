package com.taogen.datahandling.mysql.vo;

import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class SqlQueryParam {
    /**
     * Batch fetch sql must order by primaryKeyColumn. Don't need to add order by clause.
     * Batch fetch sql can't set group by clause.
     */
    private String sql;
    private Object[] args;
    /**
     * java.sql.Types
     */
    private int[] argTypes;

    private String primaryKeyColumn = "id";

    private Boolean batchFetch = false;

    private Integer batchSize = 50;

    private Integer maxSize = null;
    /**
     * TODO
     */
    private Boolean concurrentFetch = false;
}
