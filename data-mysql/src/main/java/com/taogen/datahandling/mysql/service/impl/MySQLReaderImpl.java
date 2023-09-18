package com.taogen.datahandling.mysql.service.impl;

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
        long fetchStartTime = System.currentTimeMillis();
        String sql = sqlQueryParam.getSql();
        log.info("sqlQueryParam sql is: {}", sql);
        Long count = jdbcTemplateUtils.getCount(jdbcTemplate, sqlQueryParam);
        log.info("select count: {} row(s)", count);
        List<String> labels = jdbcTemplateUtils.getQueryLabels(jdbcTemplate, sqlQueryParam);
        List<List<Object>> valuesList = new ArrayList<>();
        if (count > 0) {
            if (sqlQueryParam.getBatchFetch()) {
                valuesList = jdbcTemplateUtils.getValueListBatch(jdbcTemplate, sqlQueryParam, count);
            } else {
                valuesList = jdbcTemplateUtils.getValueList(jdbcTemplate, sqlQueryParam);
            }
        }
        log.info("values list size: {}", valuesList.size());
        if (count != valuesList.size()) {
            throw new RuntimeException("The number of rows of data does not match the actual number of rows");
        }
        log.info("fetch data cost time: {} ms", System.currentTimeMillis() - fetchStartTime);
        return new LabelAndData(labels, valuesList);
    }

}
