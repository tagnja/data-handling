package com.taogen.datahandling.mysql.service.impl;

import com.taogen.commons.datatypes.string.StringUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.mysql.service.MySQLReader;
import com.taogen.datahandling.mysql.util.JdbcTemplateUtils;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author taogen
 */
@Slf4j
@Component
public class MySQLReaderImpl implements MySQLReader {

    @Autowired
    private JdbcTemplateUtils jdbcTemplateUtils;

    @Override
    public LabelAndData read(JdbcTemplate jdbcTemplate, SqlQueryParam sqlQueryParam) {
        if (StringUtils.isBlank(sqlQueryParam.getSql())) {
            throw new RuntimeException("sql cannot be null or empty");
        }
        long fetchStartTime = System.currentTimeMillis();
        String sql = sqlQueryParam.getSql();
        log.info("sqlQueryParam sql is: {}", sql);
        List<String> labels = jdbcTemplateUtils.getQueryLabels(jdbcTemplate, sqlQueryParam);
        List<List<Object>> valuesList = new ArrayList<>();
        if (sqlQueryParam.getBatchFetch()) {
            valuesList = jdbcTemplateUtils.getValueListBatch(jdbcTemplate, sqlQueryParam);
        } else {
            valuesList = jdbcTemplateUtils.getValueList(jdbcTemplate, sqlQueryParam);
        }
        log.info("values list size: {}", valuesList.size());
        log.info("fetch data cost time: {} ms", System.currentTimeMillis() - fetchStartTime);
        return new LabelAndData(labels, valuesList);
    }

    @Override
    public List<String> getTableColumns(JdbcTemplate jdbcTemplate, String tableName) {
        if (StringUtils.isBlank(tableName)) {
            throw new RuntimeException("tableName cannot be null or empty");
        }
        String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_NAME = ?";
        List<String> columns = jdbcTemplate.queryForList(sql, String.class, tableName);
        return columns;
    }
}
