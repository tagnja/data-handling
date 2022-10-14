package com.taogen.app.export;

import com.taogen.commons.datatypes.string.StringUtils;
import com.taogen.commons.io.DirectoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Taogen
 */
@Slf4j
@Disabled
public class RuoyiUserTest extends ExportBaseTest {

    @BeforeEach
    void beforeEach() {
        showConfig();
    }

    @Test
    void addUserAccountsAndUserRoleAndDept() throws IOException {
        try (
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                        DirectoryUtils.getUserHomeDir() + "/Desktop/test.xlsx"));
                XSSFWorkbook workbook = new XSSFWorkbook(in);
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            iterator.next();
            Row row;
            while (iterator.hasNext()) {
                row = iterator.next();
                DataFormatter formatter = new DataFormatter();
                String name = formatter.formatCellValue(row.getCell(5));
                String passwd = formatter.formatCellValue(row.getCell(6));
                String leader = formatter.formatCellValue(row.getCell(3));
                String phone = formatter.formatCellValue(row.getCell(4));
                if (StringUtils.isNotEmpty(phone) && phone.indexOf("、") > 0) {
                    phone = phone.substring(0, phone.indexOf("、"));
                }
                if (doesUserExist(name)) {
                    System.out.println(name + "已存在！！");
                    continue;
                }
                log.debug(name);
                log.debug(passwd);
                String insertDeptSql = "insert into sys_dept " +
                        " (parent_id, ancestors, dept_name, order_num, leader, phone, email, create_by) " +
                        " value (100, '0,100', '${name}', 1, '${leader}', '${phone}', '', 'admin'); ";
                System.out.println(insertDeptSql
                        .replace("${name}", name)
                        .replace("${leader}", leader)
                        .replace("${phone}", phone));
                System.out.println("SELECT @deptId := LAST_INSERT_ID();");
                String insertUserSql = "insert into sys_user " +
                        " (dept_id, user_name, nick_name, phonenumber, password, create_by, words_left, yuqing_group_id) " +
                        " value (@deptId, '${name}', '${name}', '${phone}', '${passwd}', 'admin', '30000', '214');";
                System.out.println(insertUserSql
                        .replace("${name}", name)
                        .replace("${passwd}", encryptPassword(passwd))
                        .replace("${leader}", leader)
                        .replace("${phone}", phone));
                System.out.println("SELECT @userId := LAST_INSERT_ID();");
                String insertUserRoleSql = "insert into sys_user_role " +
                        " (user_id, role_id) " +
                        " value (@userId, 2);";
                System.out.println(insertUserRoleSql);
                System.out.println();
            }
        }
    }

    private boolean doesUserExist(String name) {
        Map<String, Object> countMap = jdbcTemplate.queryForMap("select count(*) as count from sys_user where del_flag = '0' and user_name = '" + name + "'");
        Integer count = Integer.parseInt(countMap.get("count").toString());
        return count > 0;
    }

    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }
}
