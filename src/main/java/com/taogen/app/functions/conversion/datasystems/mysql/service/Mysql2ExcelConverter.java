package com.taogen.app.functions.conversion.datasystems.mysql.service;

import com.taogen.app.functions.conversion.datasystems.mysql.vo.SqlQueryParam;

import java.io.IOException;

/**
 * @author Taogen
 */
public interface Mysql2ExcelConverter {
    String convertSql2ExcelV1(SqlQueryParam sqlQueryParam,
                              String outputFileDir,
                              String outputFileName) throws IOException;
}
