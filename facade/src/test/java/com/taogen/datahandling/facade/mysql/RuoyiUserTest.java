package com.taogen.datahandling.facade.mysql;

import com.taogen.commons.datatypes.string.StringUtils;
import com.taogen.commons.office.poi.ExcelUtils;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Disabled
    void addUserAndUserRoleAndDeptToBubbleDanyangSys() throws IOException {
        // parameter begin
        boolean executeSql = false;
        String filePath = getExportDirPath() + "test.xlsx";
        int nameColNum = 1, passwdColNum = 2, leaderColNum = 3, phoneColNum = 4;
        // parameter end
        String fileSuffix = filePath.substring(filePath.lastIndexOf("."));
        try (
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));
                Workbook workbook = ExcelUtils.createWorkbookByFileSuffix(in, fileSuffix)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            iterator.next();
            Row row;
            List<String> sqlList = new ArrayList<>();
            int totalUserNum = 0, existedUserNum = 0;
            while (iterator.hasNext()) {
                row = iterator.next();
                DataFormatter formatter = new DataFormatter();
                String name = formatter.formatCellValue(row.getCell(nameColNum)).trim();
                if (StringUtils.isEmpty(name)) {
                    break;
                }
                String passwd = formatter.formatCellValue(row.getCell(passwdColNum)).trim();
                String leader = formatter.formatCellValue(row.getCell(leaderColNum)).trim();
                String phone = formatter.formatCellValue(row.getCell(phoneColNum)).trim();
                log.debug("name: {}, passwd: {}, leader: {}, phone: {}", name, passwd, leader, phone);
                if (StringUtils.isNotEmpty(phone) && phone.indexOf("、") > 0) {
                    phone = phone.substring(0, phone.indexOf("、"));
                }
                if (doesUserExist(name)) {
                    if (executeSql) {
                        updateUserPassword(name, passwd);
                    }
                    log.info("{} already exists!", name);
                    existedUserNum++;
                    continue;
                }
                sqlList.addAll(getExecSqlList(name, passwd, phone, leader));
                totalUserNum++;
            }
            log.info("total user num: {}", totalUserNum);
            log.info("existed user num: {}", existedUserNum);
            log.info("generated sql list size is {}", sqlList.size());
            log.info("generated sql list printed below \n{}", sqlList.stream().collect(Collectors.joining("\r\n")));
            if (executeSql) {
                execSqlList(sqlList);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    void execSqlList(List<String> sqlList) {
        for (String sql : sqlList) {
            log.debug("executed sql is {}", sql);
            jdbcTemplate.execute(sql);
        }
    }

    private List<String> getExecSqlList(String name, String passwd, String phone, String leader) {
        List<String> sqlList = new ArrayList<>();
        String insertDeptSql = "insert into sys_dept " +
                " (parent_id, ancestors, dept_name, order_num, leader, phone, email, create_by) " +
                " value (100, '0,100', '${name}', 1, '${leader}', '${phone}', '', 'admin'); ";
        insertDeptSql = insertDeptSql
                .replace("${name}", name)
                .replace("${leader}", leader)
                .replace("${phone}", phone);
        sqlList.add(insertDeptSql);
        String deptIdSql = "SELECT @deptId := LAST_INSERT_ID();";
        sqlList.add(deptIdSql);
        String insertUserSql = "insert into sys_user " +
                " (dept_id, user_name, nick_name, phonenumber, password, create_by, words_left, yuqing_group_id) " +
                " value (@deptId, '${name}', '${name}', '${phone}', '${passwd}', 'admin', '30000', '214');";
        insertUserSql = insertUserSql
                .replace("${name}", name)
                .replace("${passwd}", encryptPassword(passwd))
                .replace("${leader}", leader)
                .replace("${phone}", phone);
        sqlList.add(insertUserSql);
        String userIdSql = "SELECT @userId := LAST_INSERT_ID();";
        sqlList.add(userIdSql);
        String insertUserRoleSql = "insert into sys_user_role " +
                " (user_id, role_id) " +
                " value (@userId, 2);";
        sqlList.add(insertUserRoleSql);
        return sqlList;
    }

    private void updateUserPassword(String username, String passwd) {
        passwd = encryptPassword(passwd);
        String sql = "update sys_user set password = '" + passwd + "' where user_name = '" + username + "'";
        int update = jdbcTemplate.update(sql);
        log.debug("executed sql is {}", sql);
        log.info("update {} user passwords", update);
    }

    private boolean doesUserExist(String username) {
        Map<String, Object> countMap = jdbcTemplate.queryForMap("select count(*) as count from sys_user " +
                "where del_flag = '0' and user_name = '" + username + "'");
        Integer count = Integer.parseInt(countMap.get("count").toString());
        return count > 0;
    }

    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }
}
