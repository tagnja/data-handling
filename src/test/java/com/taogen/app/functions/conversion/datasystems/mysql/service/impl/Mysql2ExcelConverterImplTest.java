package com.taogen.app.functions.conversion.datasystems.mysql.service.impl;

import com.taogen.app.SpringBootBaseTest;
import com.taogen.app.functions.conversion.datasystems.mysql.service.Mysql2ExcelConverter;
import com.taogen.app.functions.conversion.datasystems.mysql.vo.SqlQueryParam;
import com.taogen.commons.datatypes.datetime.DateFormatters;
import com.taogen.commons.io.DirectoryUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

class Mysql2ExcelConverterImplTest extends SpringBootBaseTest {

    @Autowired
    private Mysql2ExcelConverter mysql2ExcelConverter;

    @Test
    void convertSql2Excel() throws IOException, SQLException {
        String selectTableSql = "select\n" +
                "TABLE_SCHEMA as tableSchema,\n" +
                "TABLE_NAME as tableName,\n" +
                "TABLE_COMMENT as tableComment,\n" +
                "CREATE_TIME as createTime,\n" +
                "UPDATE_TIME as updateTime\n" +
                "from information_schema.tables \n" +
                "limit ?";
        Object[] args = new Object[]{new Integer(10)};
        int[] argTypes = new int[]{Types.INTEGER};
        SqlQueryParam sqlQueryParam = new SqlQueryParam();
        sqlQueryParam.setSql(selectTableSql);
        sqlQueryParam.setArgs(args);
        sqlQueryParam.setArgTypes(argTypes);
        String outputDir = DirectoryUtils.getTempDir();
        String outputFileName = "Mysql2ExcelConverterImplTest.convertSql2Excel-%s.xlsx";
        String outputFilePath = mysql2ExcelConverter.convertSql2ExcelV1(
                sqlQueryParam,
                outputDir,
                String.format(outputFileName, DateFormatters.yyyy_MM_dd_HH_mm_ss_2.format(new Date()))
        );
    }
}