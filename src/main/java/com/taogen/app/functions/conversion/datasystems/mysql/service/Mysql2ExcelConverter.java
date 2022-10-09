package com.taogen.app.functions.conversion.datasystems.mysql.service;

import com.taogen.app.functions.conversion.datasystems.mysql.vo.SqlQueryParam;
import com.taogen.app.functions.conversion.datasystems.mysql.vo.TableLabelAndData;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Taogen
 */
public interface Mysql2ExcelConverter {
    TableLabelAndData getTableLabelsAndData(SqlQueryParam sqlQueryParam);
    String writeTableLabelAndDataToExcel(TableLabelAndData tableLabelAndData, String outputFileDir, String outputFileName) throws IOException;

    String convertSql2ExcelV1(SqlQueryParam sqlQueryParam,
                              String outputFileDir,
                              String outputFileName) throws IOException;

    TableLabelAndData executeQuery(String sql) throws SQLException;
}
