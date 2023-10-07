package com.taogen.datahandling.mysql.util;

import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author taogen
 */
@Slf4j
@Component
public class JdbcTemplateUtils {
    /**
     * set startId to fetch a page of data
     * Note: batch query sql must "order by id"
     *
     * @param sqlQueryParam
     * @param count
     * @return
     */
    public List<List<Object>> getValueListBatch(JdbcTemplate jdbcTemplate,
                                                SqlQueryParam sqlQueryParam,
                                                Long count) {
        String sql = sqlQueryParam.getSql();
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        Integer batchSize = sqlQueryParam.getBatchSize();
        String selectFirstRowSql = MysqlUtils.wrapperQueryWithOrderByAndLimit(sql, sqlQueryParam.getPrimaryKeyColumn(), 1L);
        log.debug("select first row sql: {}", selectFirstRowSql);
        Map<String, Object> firstRow = jdbcTemplate.queryForMap(selectFirstRowSql, args, argTypes);
        log.debug("first row: {}", firstRow);
        Long firstRowId = Long.valueOf(firstRow.get(sqlQueryParam.getPrimaryKeyColumn()).toString());
        Long startId = firstRowId - 1;
        List<List<Object>> resultValueList = new ArrayList<>();
        int start = 0;
        while (start < count) {
            long size = batchSize > (count - start) ? (count - start) : batchSize;
            String primaryKeyPredicate = new StringBuilder()
                    .append(" ")
                    .append(sqlQueryParam.getPrimaryKeyColumn())
                    .append(" > ")
                    .append(startId)
                    .append(" order by ")
                    .append(sqlQueryParam.getPrimaryKeyColumn())
                    .toString();
            String batchSelectSql = MysqlUtils.wrapperPredicateToSql(sql, primaryKeyPredicate);
            batchSelectSql = MysqlUtils.wrapperQueryToSelectLimitSize(batchSelectSql, size);
            log.debug("batch select sql: {}", batchSelectSql);
            SqlRowSet sqlRowSet;
            if (argTypes != null) {
                sqlRowSet = jdbcTemplate.queryForRowSet(batchSelectSql, args, argTypes);
            } else {
                sqlRowSet = jdbcTemplate.queryForRowSet(batchSelectSql, args);
            }
            SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
            int columnNum = metaData.getColumnCount();
            List<List<Object>> queryResultData = getQueryResultData(sqlRowSet, columnNum);
            resultValueList.addAll(queryResultData);
            start += batchSize;
            startId = Long.valueOf(queryResultData.get(queryResultData.size() - 1).get(0).toString());
        }
        return resultValueList;
    }

    private List<List<Object>> getQueryResultData(SqlRowSet sqlRowSet, int columnNum) {
        List<List<Object>> valuesList = new ArrayList<>();
        while (sqlRowSet.next()) {
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < columnNum; i++) {
                Object value = sqlRowSet.getObject(i + 1);
                values.add(value);
            }
            valuesList.add(values);
        }
        return valuesList;
    }

    public List<List<Object>> getValueList(JdbcTemplate jdbcTemplate, SqlQueryParam sqlQueryParam) {
        String sql = sqlQueryParam.getSql();
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        SqlRowSet sqlRowSet;
        if (argTypes != null) {
            sqlRowSet = jdbcTemplate.queryForRowSet(sql, args, argTypes);
        } else {
            sqlRowSet = jdbcTemplate.queryForRowSet(sql, args);
        }
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        int columnNum = metaData.getColumnCount();
        return getQueryResultData(sqlRowSet, columnNum);
    }

    public List<String> getQueryLabels(JdbcTemplate jdbcTemplate, SqlQueryParam sqlQueryParam) {
        String sql = sqlQueryParam.getSql();
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        String selectLabelSql = MysqlUtils.wrapperQueryToSelectLimitSize(sql, 0L);
        log.debug("select label sql: {}", selectLabelSql);
        SqlRowSet sqlRowSet;
        if (argTypes != null) {
            sqlRowSet = jdbcTemplate.queryForRowSet(selectLabelSql, args, argTypes);
        } else {
            sqlRowSet = jdbcTemplate.queryForRowSet(selectLabelSql, args);
        }
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        return getQueryLabelsByMetaData(metaData);
    }


    private List<String> getQueryLabelsByMetaData(SqlRowSetMetaData sqlRowSetMetaData) {
        List<String> labels = new ArrayList<>();
        int columnNum = sqlRowSetMetaData.getColumnCount();
        for (int i = 0; i < columnNum; i++) {
            labels.add(sqlRowSetMetaData.getColumnLabel(i + 1));
        }
        return labels;
    }

    public Long getCount(JdbcTemplate jdbcTemplate, SqlQueryParam sqlQueryParam) {
        String sql = sqlQueryParam.getSql();
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        String selectCountSql = MysqlUtils.wrapperQueryToSelectCount(sql);
        log.info("select count sql: {}", selectCountSql);
        Map<String, Object> selectCountResult;
        if (argTypes != null) {
            selectCountResult = jdbcTemplate.queryForMap(selectCountSql, args, argTypes);
        } else {
            selectCountResult = jdbcTemplate.queryForMap(selectCountSql, args);
        }
        return Long.valueOf(selectCountResult.get("count").toString());
    }

}
