package com.taogen.datahandling.mysql.service;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @author taogen
 */
public interface MySQLWriter {
    void write(JdbcTemplate jdbcTemplate, String tableName, List<String> columns, List<List<Object>> data);
}
