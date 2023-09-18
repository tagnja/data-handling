package com.taogen.datahandling.mysql.service;

import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author taogen
 */
public interface MySQLReader {
    LabelAndData read(JdbcTemplate jdbcTemplate, SqlQueryParam sqlQueryParam);
}
