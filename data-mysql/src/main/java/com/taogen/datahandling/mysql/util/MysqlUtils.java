package com.taogen.datahandling.mysql.util;

/**
 * @author Taogen
 */
public class MysqlUtils {
    public static String wrapperQueryToSelectCount(String sql) {
        return new StringBuilder()
                .append("SELECT count(*) as count FROM (")
                .append(sql)
                .append(") as t")
                .append(System.currentTimeMillis())
                .toString();
    }

    public static String wrapperQueryWithOrderByAndLimit(String sql, String orderBy, Long limitSize) {
        return new StringBuilder()
                .append("SELECT * FROM (")
                .append(sql)
                .append(") as t")
                .append(System.currentTimeMillis())
                .append(" order by ")
                .append(orderBy)
                .append(" limit ")
                .append(limitSize)
                .toString();
    }

    public static String wrapperQueryToSelectLimitSize(String sql, Long limitSize) {
        return new StringBuilder()
                .append("SELECT * FROM (")
                .append(sql)
                .append(") as t")
                .append(System.currentTimeMillis())
                .append(" limit ")
                .append(limitSize)
                .toString();
    }

    public static String wrapperPredicateToSql(String sql, String predicate) {
        return new StringBuilder()
                .append("SELECT * FROM (")
                .append(sql)
                .append(") as t")
                .append(System.currentTimeMillis())
                .append(" where ")
                .append(predicate)
                .toString();
    }
}
