package com.taogen.datahandling.facade.service;

import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;

/**
 * @author taogen
 */
public interface Sql2ExcelConverter {
    void convert(JdbcTemplate jdbcTemplate, SqlQueryParam sqlQueryParam, String filePath) throws IOException;
}
