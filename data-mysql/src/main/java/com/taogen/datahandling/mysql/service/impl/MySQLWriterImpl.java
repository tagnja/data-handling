package com.taogen.datahandling.mysql.service.impl;

import com.taogen.commons.dataaccess.MySQLUtils;
import com.taogen.datahandling.mysql.service.MySQLWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author taogen
 */
@Slf4j
@Component
public class MySQLWriterImpl implements MySQLWriter {
    @Override
    public void write(JdbcTemplate jdbcTemplate, String tableName, List<String> columns, List<List<Object>> data) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("insert into ").append(tableName).append(" \n");
        sqlBuilder.append("(");
        for (String column : columns) {
            sqlBuilder.append(column).append(",");
        }
        sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
        sqlBuilder.append(") values\n");
        for (List<Object> list : data) {
            sqlBuilder.append("(");
            for (int i = 0; i < columns.size(); i++) {
                if (i >= list.size()) {
                    sqlBuilder.append("null,");
                    continue;
                }
                String value = MySQLUtils.escapeSpecialCharsForColumnValue(list.get(i).toString());
                sqlBuilder.append("'").append(value).append("',");
            }
            sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
            sqlBuilder.append("),\n");
        }
        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
        jdbcTemplate.execute(sqlBuilder.toString());
    }
}
