package com.taogen.datahandling.mysql.util;

import com.taogen.commons.collection.CollectionUtils;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author taogen
 */
@Slf4j
@Component
public class JdbcTemplateUtils {

    public static final Pattern SELECT_ID_PATTERN = Pattern.compile("select.*\\s(\\w+[.])*id\\s*[,]*.*\\sfrom\\s", Pattern.CASE_INSENSITIVE);

    public static final Pattern GROUP_BY_PATTERN = Pattern.compile("group\\s+by", Pattern.CASE_INSENSITIVE);

    public static final Pattern ORDER_BY_PATTERN = Pattern.compile("order\\s+by", Pattern.CASE_INSENSITIVE);

    public static final Pattern TABLE_ALIAS_PATTERN = Pattern.compile("from\\s+(\\w|[.])+\\s+(as\\s+)*(\\w+)", Pattern.CASE_INSENSITIVE);

    private static void handleSqlQueryParam(SqlQueryParam sqlQueryParam) {
        String sql = sqlQueryParam.getSql();
        Matcher matcher = TABLE_ALIAS_PATTERN.matcher(sql);
        if (matcher.find()) {
            sqlQueryParam.setPrimaryKeyColumn(matcher.group(3) + "." + sqlQueryParam.getPrimaryKeyColumn());
        }
    }

    /**
     * set startId to fetch a page of data
     * Note: batch query sql must "order by id"
     *
     * @param sqlQueryParam
     * @return
     */
    public List<List<Object>> getValueListBatch(JdbcTemplate jdbcTemplate,
                                                SqlQueryParam sqlQueryParam
    ) {
        String sql = sqlQueryParam.getSql();
        checkSql(sql);
        handleSqlQueryParam(sqlQueryParam);
        Object[] args = sqlQueryParam.getArgs();
        int[] argTypes = sqlQueryParam.getArgTypes();
        long startId = 0;
        List<List<Object>> resultValueList = new ArrayList<>();
        while (true) {
            String batchSelectSql = getBatchSelectSql(sqlQueryParam, startId);
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
            if (CollectionUtils.isEmpty(queryResultData) ||
                    (sqlQueryParam.getMaxSize() != null &&
                            resultValueList.size() >= sqlQueryParam.getMaxSize())) {
                break;
            }
            resultValueList.addAll(queryResultData);
            startId = Long.valueOf(queryResultData.get(queryResultData.size() - 1).get(0).toString()) + 1;
        }
        if (sqlQueryParam.getMaxSize() != null && resultValueList.size() > sqlQueryParam.getMaxSize()) {
            resultValueList = resultValueList.subList(0, sqlQueryParam.getMaxSize());
        }
        return resultValueList;
    }

    private void checkSql(String sql) {
        Matcher selectIdMatcher = SELECT_ID_PATTERN.matcher(sql);
        if (!selectIdMatcher.find()) {
            throw new RuntimeException("Batch query sql must select id column");
        }
        Matcher groupByMatcher = GROUP_BY_PATTERN.matcher(sql);
        if (groupByMatcher.find()) {
            throw new RuntimeException("Batch query sql must not contain group by");
        }
        Matcher orderBymatcher = ORDER_BY_PATTERN.matcher(sql);
        if (orderBymatcher.find()) {
            throw new RuntimeException("Batch query sql must not contain order by. Batch fetch sql must order by primaryKeyColumn. Don't need to add order by clause.");
        }
    }

    private String getBatchSelectSql(SqlQueryParam sqlQueryParam, long startId) {
        String sql = sqlQueryParam.getSql();
        sql = addStartIdToSql(sql, sqlQueryParam.getPrimaryKeyColumn(), startId);
        sql = sql + " order by " + sqlQueryParam.getPrimaryKeyColumn() + " asc ";
        sql = sql + " limit " + sqlQueryParam.getBatchSize();
        return sql;
    }

    private String addStartIdToSql(String sql, String primaryKeyColumn, long startId) {
        if (sql.toLowerCase().contains("where")) {
            sql = sql.replace("where", "where " + primaryKeyColumn + " >= " + startId + " and ( ") + " ) ";
        } else {
            sql = sql + " where " + primaryKeyColumn + " >= " + startId;
        }
        return sql;
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
        if (sqlQueryParam.getMaxSize() != null) {
            sql = MysqlUtils.wrapperQueryToSelectLimitSize(sql, Long.valueOf(sqlQueryParam.getMaxSize()));
        }
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
