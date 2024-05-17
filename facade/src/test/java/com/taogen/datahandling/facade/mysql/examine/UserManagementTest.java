package com.taogen.datahandling.facade.mysql.examine;

import com.taogen.commons.crypto.HashUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author taogen
 */
@Slf4j
public class UserManagementTest extends ExportBaseTest {

    @Test
    void insertExamineOnlineAccount() throws IOException, InvalidFormatException {
        LabelAndData labelAndData = excelReader.read(Arrays.asList(getExportDirPath() + "梅州蕉岭新媒体.xlsx"));
        List<List<Object>> data = labelAndData.getValuesList();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String current = dateFormat.format(new Date());
        System.out.println("SET autocommit = 0;  \n" +
                "START TRANSACTION;");
        for (List<Object> row : data) {
            String phone = row.get(1).toString();
            String name = row.get(0).toString();
            String password = phone.substring(phone.length() - 6);
            Integer wordsLimit = 100000;
            Integer writingLimit = 30;
            Integer transferLimit = 30;
            String endDate = "2024-08-31";
            String customerName = "梅州蕉岭融媒中心";
            Integer areaId = 441427;
            Integer roleId = 124;
            Integer customerType = 1;
            Integer deptId = 157;
            Integer type = 2;
            String remark = "批量开审核账号-" + current;
            String insertExamineUserSql = "insert into examine.sys_user " +
                    "(dept_id, user_name, nick_name, phonenumber, password, status, " +
                    "type,  create_by, end_date, remark, customer_name, customer_type, area_id, words_length, words_limit, transform_num, transform_limit, writing_generate_num, writing_generate_limit) values \n" +
                    "(" + deptId + ", '" + phone + "', '" + name + "', '" + phone + "', '" + new BCryptPasswordEncoder().encode(HashUtils.md5(password).toUpperCase()) + "', 3, " +
                    type + ", 1, '" + endDate + "','" + remark + "', '" + customerName + "', " + customerType + ", " + areaId + ", " + wordsLimit + "," + wordsLimit + ", " + transferLimit + "," + transferLimit + "," + writingLimit + "," + writingLimit + " );\n";
            System.out.println(insertExamineUserSql);
            String examineUserId = "SELECT @examineUserId := LAST_INSERT_ID();\n";
            System.out.println(examineUserId);
            String updateExamineUserSql = "update examine.sys_user set yuqing_group_id = CONCAT('" + type + "-',  @examineUserId) where user_id = @examineUserId;";
            System.out.println(updateExamineUserSql);
            String insertSysUserRoleSql = "insert into examine.sys_user_role (user_id, role_id) values \n" +
                    "(@examineUserId, " + roleId + ");\n";
            System.out.println(insertSysUserRoleSql);
        }
        System.out.println("COMMIT;\n" +
                "SET autocommit = 1;\n" +
                "-- ROLLBACK;");
    }
}
