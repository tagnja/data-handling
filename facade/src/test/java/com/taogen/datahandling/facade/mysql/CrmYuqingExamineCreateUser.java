package com.taogen.datahandling.facade.mysql;

import com.taogen.commons.RandomUtils;
import com.taogen.commons.crypto.HashUtils;
import com.taogen.commons.datatypes.string.StringUtils;
import com.taogen.commons.office.poi.ExcelUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 账号已存在检查和执行策略选择
 * Excel 空行问题，数据重复问题
 * 一个客户，多个账号问题
 *
 * @author Taogen
 */
@Slf4j
@Disabled
public class CrmYuqingExamineCreateUser extends ExportBaseTest {

    public static void main(String[] args) {
        String password = RandomUtils.generateRandomAlphanumericStr(12);
        log.debug("password: " + password);
        System.out.println(generateYuqingExamineUser(3383,
                "抚州钟岭街道办事处",
                "抚州高新技术产业开发区钟岭街道办事处",
                password));

    }

    /**
     * If customer exists in Yuqing, skip
     * If customer doesn't exist in Yuqing, collect Map customerId => customerId, customerName, userName, password
     * generate sql by Map customerId => customerId, customerName, userName, password
     * append password to excel
     *
     * @param customerName
     * @param userName
     * @return password
     */
    static String generateYuqingExamineUser(Integer customerId,
                                            String customerName,
                                            String userName,
                                            String password) {
        String endDate = "2023-06-30";
        /**
         * name: from Excel
         * pass: MD(random).toUpperCase
         */
        // insert yuqing
        String insertCustomerInfoSql = "insert into console_data.customer_info " +
                "(id, name, sys_name, sys_icon, user_type, end_date) values \n" +
                "(" + customerId + ",'" + customerName + "', '舆情监测平台', " +
                "'http://yuwoyg.oss-cn-hangzhou.aliyuncs.com/yuqing_project/sys_icon/770c8727-0120-4b18-bfac-a8f0b851e54e.png'," +
                "0, '" + endDate + "');\n";
        System.out.println(insertCustomerInfoSql);
        String insertYuqingUserSql = "insert into console_data.sys_user " +
                "(name, pass, end_date, status, customer_id, group_id) values \n" +
                "('" + userName + "', '" + HashUtils.md5(password).toUpperCase() + "', '" + endDate + "', 1, " + customerId + ", 149);\n";
        System.out.println(insertYuqingUserSql);
        String yuqingUserId = "SELECT @yuqingUserId := LAST_INSERT_ID();\n";
        System.out.println(yuqingUserId);
        String insertYuqingUserRoleSql = "insert into console_data.sys_user_role " +
                "(user_id, role_id) values \n" +
                "(@yuqingUserId, 7);\n";
        System.out.println(insertYuqingUserRoleSql);
        // insert examine
        String insertDeptSql = "insert into examine.sys_dept " +
                "(parent_id, ancestors, dept_name, examine_limit, transform_limit, customer_id, is_formal, " +
                "end_date, writing_generate_limit) values \n" +
                "(0, '0', '" + customerName + "', 200000, 30, " + customerId + ", 0, '" + endDate + "' ,0);\n";
        System.out.println(insertDeptSql);
        String deptId = "SELECT @deptId := LAST_INSERT_ID();\n";
        System.out.println(deptId);
        String insertExamineUserSql = "insert into examine.sys_user " +
                "(dept_id, user_name, nick_name, user_type, password, status, " +
                "type, yuqing_user_id, yuqing_user_type, yuqing_group_id, end_date) values \n" +
                "(@deptId, '" + userName + "', '" + userName + "', 1, '" + new BCryptPasswordEncoder().encode(HashUtils.md5(password).toUpperCase()) + "', 0, " +
                "1, @yuqingUserId, 0, 149, '" + endDate + "');\n";
        System.out.println(insertExamineUserSql);
        String examineUserId = "SELECT @examineUserId := LAST_INSERT_ID();\n";
        System.out.println(examineUserId);
        String insertSysUserRoleSql = "insert into sys_user_role (user_id, role_id) values \n" +
                "(@examineUserId, 2), (@examineUserId, 126);\n";
        System.out.println(insertSysUserRoleSql);
        return password;
    }

    @BeforeEach
    void beforeEach() {
        showConfig();
    }

    @Test
    @Disabled
    void selectBasicInfo() {
        String countyName = "xxx";
        String sustainUser = "xxx";
        String saleUser = "xxx";
        String queryCrmUser = "select *\n" +
                "from crm_user\n" +
                "where name in (\"" + saleUser + "\", \"" + sustainUser + "\");";
        List<Map<String, Object>> users = jdbcTemplate.queryForList(queryCrmUser);
        System.out.println(users.stream().map(Object::toString).collect(Collectors.joining("\n")));
        String queryCrmArea = "select a.code as aCode, a.`name` as aName, b.`code` as bCode, b.name as bName, c.`code` as cCode, c.name as cName  from crm_area_county as a  \n" +
                "left join crm_area_city as b on a.city_code=b.`code`\n" +
                "left join crm_area_province as c on b.province_code=c.`code`\n" +
                "where  a.`name` like \"" + countyName + "%\"";
        List<Map<String, Object>> area = jdbcTemplate.queryForList(queryCrmArea);
        System.out.println(area);
    }

    @Test
    @Disabled
    void addCustomerToCrm() throws IOException {
        /**
         * - 名称：（Excel 获取）
         * - 地区：江西省 360000，抚州市 361000，市辖区 361001
         * - 类型：其他 52
         * - 产品类型：审核 1
         * - 客户状态：试用 0
         * - 支撑：xxx 17
         * - 销售：xxx 64
         * - 备注：批量开审核账号
         */
        Integer sustainUserId = 50;
        Integer saleUserId = 86;
        String provinceCode = "320000";
        String cityCode = "321100";
        String countyCode = "321103";
        String filePath = getExportDirPath() + "镇江.xlsx";
        Integer sheetNum = 0; // start from 0
        Integer startRow = 1; // start from 0
        Integer columnNum = 1; // start from 0
        Set<String> names = getNamesFromExcel(filePath, sheetNum, startRow, columnNum);
        String selectExistCustomerNames = "select name from crm_customer where name in (" + names.stream().map(item -> "'" + item + "'").collect(Collectors.joining(",")) + ")\n";
        System.out.println(selectExistCustomerNames);
//        String name = "抚州钟岭街道办事处";
        String insertCustomerBatchSql = getInsertCustomerBatchSql(
                names, provinceCode, cityCode, countyCode, sustainUserId, saleUserId);
    }

    @Test
    @Disabled
    void appendCustomerId() throws IOException {
        String inputFilePath = getExportDirPath() + "南通通州教体局-accounts.xlsx";
        Integer customerNameColNum = 0;
        Integer appendCustomerIdColNum = 4;
        DataFormatter formatter = new DataFormatter();
        Set<String> totalNames = new LinkedHashSet<>();
        Set<String> insertNames = new LinkedHashSet<>();
        Consumer<Row> rowsModifyConsumer = row -> {
//            Cell userNameCell = row.getCell(0);
            Cell customerNameCell = row.getCell(customerNameColNum);
            String customerName = formatter.formatCellValue(customerNameCell).trim();
            if (totalNames.contains(customerName)) {
                log.debug("repeated customerName: {}", customerName);
            }
            totalNames.add(customerName);
            try {
                Map<String, Object> map = jdbcTemplate.queryForMap("select id, name from crm.crm_customer where name = '" + customerName.trim() + "'");
                if (map != null && map.get("id") != null) {
                    Cell cell = row.createCell(appendCustomerIdColNum);
                    cell.setCellValue(map.get("id").toString());
                }
            } catch (EmptyResultDataAccessException e) {
                log.info("no record for {}", customerName);
                insertNames.add(customerName);
            }
        };
        String outputFile = excelModifier.modifyRows(inputFilePath, 0, rowsModifyConsumer);
        log.info("output file: {}", outputFile);
        System.out.println("totalCustomer size: " + totalNames.size());
//        getInsertCustomerBatchSql(insertNames, provinceCode, cityCode, countyCode, sustainUserId, saleUserId);
    }

    @ParameterizedTest
    @Disabled
    @CsvSource("2023-11-01 00:00:00, 2023-11-31 23:59:59")
    void updateAndAppendExamineUse(String startTime, String endTime) throws IOException {
        String inputFilePath = getExportDirPath() + "通州.xlsx";
        Integer userNameColNum = 1;
        Integer appendUseCountCol = 3;
        Integer appendUseCharNumCol = 4;
        DataFormatter formatter = new DataFormatter();
//        Set<String> totalNames = new LinkedHashSet<>();
//        Set<String> insertNames = new LinkedHashSet<>();
        Consumer<Row> rowsModifyConsumer = row -> {
//            Cell userNameCell = row.getCell(0);
            Cell userNameCell = row.getCell(userNameColNum);
            String userName = formatter.formatCellValue(userNameCell).trim();
            String query = "select user.user_name \"账号名称\", count(*) \"消费次数\", SUM(LENGTH) as \"消费字数\"\n" +
                    "from examine.examine_deposit_record as deposit \n" +
                    "right join examine.sys_user as user on deposit.create_by = user.user_id\n" +
                    "WHERE\n" +
                    "deposit.create_time between '" + startTime + "' and '" + endTime + "' and \n" +
                    "deposit.type in (1, 2, 5) and\n" +
                    "deposit.`length` < 0 and \n" +
                    "user.user_name = \"" + userName + "\"";
            Map<String, Object> result = jdbcTemplate.queryForMap(query);

            if (result.get("消费次数") != null) {
                row.createCell(appendUseCountCol).setCellValue(result.get("消费次数").toString());
            } else {
                row.createCell(appendUseCountCol).setCellValue(0);
            }
            if (result.get("消费字数") != null) {
                row.createCell(appendUseCharNumCol).setCellValue(result.get("消费字数").toString());
            } else {
                row.createCell(appendUseCharNumCol).setCellValue(0);
            }

//            if (totalNames.contains(userName)) {
//                log.debug("repeated userName: {}", userName);
//            }
//            totalNames.add(userName);
//            String password = RandomUtils.generateRandomAlphanumericStr(12);
//            String updateYuqingPassword = "update console_data.sys_user set pass='" + HashUtils.md5(password).toUpperCase() + "' where name='" + userName + "';";
//            System.out.println(updateYuqingPassword);
//            String updateExaminePassword = "update examine.sys_user set password='" + new BCryptPasswordEncoder().encode(HashUtils.md5(password).toUpperCase()) + "' where user_name='" + userName + "';";
//            System.out.println(updateExaminePassword);
//            Cell cell = row.createCell(passwordColNum);
//            cell.setCellValue(password);
//            row.createCell(passwordColNum+1).setCellValue(userName);
        };
        String outputFile = excelModifier.modifyRows(inputFilePath, 1, rowsModifyConsumer);
        log.info("output file: {}", outputFile);
//        System.out.println("totalCustomer size: " + totalNames.size());
//        getInsertCustomerBatchSql(insertNames, provinceCode, cityCode, countyCode, sustainUserId, saleUserId);
    }

    @Test
    @Disabled
    void updateAndAppendPassword() throws IOException {
        String inputFilePath = getExportDirPath() + "泰州高港_2023-03-02_16-47-09-652.xlsx";
        Integer customerNameColNum = 1;
        Integer userNameColNum = 2;
        Integer appendCustomerIdColNum = 4;
        Integer passwordColNum = 3;
        DataFormatter formatter = new DataFormatter();
        Set<String> totalNames = new LinkedHashSet<>();
        Set<String> insertNames = new LinkedHashSet<>();
        Consumer<Row> rowsModifyConsumer = row -> {
//            Cell userNameCell = row.getCell(0);
            Cell userNameCell = row.getCell(userNameColNum);
            String userName = formatter.formatCellValue(userNameCell).trim();
            if (totalNames.contains(userName)) {
                log.debug("repeated userName: {}", userName);
            }
            totalNames.add(userName);
            String password = RandomUtils.generateRandomAlphanumericStr(12);
            String updateYuqingPassword = "update console_data.sys_user set pass='" + HashUtils.md5(password).toUpperCase() + "' where name='" + userName + "';";
            System.out.println(updateYuqingPassword);
            String updateExaminePassword = "update examine.sys_user set password='" + new BCryptPasswordEncoder().encode(HashUtils.md5(password).toUpperCase()) + "' where user_name='" + userName + "';";
            System.out.println(updateExaminePassword);
            Cell cell = row.createCell(passwordColNum);
            cell.setCellValue(password);
            row.createCell(passwordColNum + 1).setCellValue(userName);
        };
        String outputFile = excelModifier.modifyRows(inputFilePath, 1, rowsModifyConsumer);
        log.info("output file: {}", outputFile);
        System.out.println("totalCustomer size: " + totalNames.size());
//        getInsertCustomerBatchSql(insertNames, provinceCode, cityCode, countyCode, sustainUserId, saleUserId);
    }

    @Test
    void removeYuqingAndExamineUserByUsername() throws IOException, InvalidFormatException {
        LabelAndData read = excelReader.read(Arrays.asList(getExportDirPath() + "/南通通州教体局-accounts.xlsx"));
        for (int i = 0; i < read.getValuesList().size(); i++) {
            Object userName = read.getValuesList().get(i).get(1);
            System.out.println(userName);
            // Add createTime condition
            jdbcTemplate.execute("delete from console_data.sys_user where name='" + userName + "' and gmt_create > '2024-03-18 14:00:00';");
            // Add lastId condition
            jdbcTemplate.execute("delete from examine.sys_user where user_name='" + userName + "' and user_id >= 3175;");
        }

    }


    /**
     * 根据舆情客户，批量开审核账号
     * <p>
     * 舆情账号：
     * - 密码随机
     * - 空组。
     * - 禁用
     * 舆情账号角色：
     * - 标准版
     * 审核部门：客户名称
     * 审核账号：
     * - 空组
     * - 随机密码
     * - 审核字数：10万字
     * 账号角色：
     * - 普通角色、问问小舆
     *
     * @throws IOException
     */
    @Test
    @Disabled
    void AddUserToYuqingAndExamine2() throws IOException {
        String inputFilePath = getExportDirPath() + "南通通州教体局-accounts_2024-03-18_standard.xlsx";
        String endDate = "2024-04-30";
        Integer characterNum = 100000;
        Integer writingNum = 20;
        Integer docConvertNum = 20;
        // first column of Excel is 0
        Integer customerNameColNum = 0;
        Integer userNameColNum = 1;
        Integer passwordColNum = 2;
        Integer customerIdColNum = 3;
        DataFormatter formatter = new DataFormatter();
        Set<String> totalUserNames = new LinkedHashSet<>();
        Set<String> insertNames = new LinkedHashSet<>();
        Map<String, InsertUserInfo> map = new LinkedHashMap<>();
        Consumer<Row> rowsModifyConsumer = row -> {
            Cell customerNameCell = row.getCell(customerNameColNum);
            String customerName = formatter.formatCellValue(customerNameCell).trim();
            Cell userNameCell = row.getCell(userNameColNum);
            String userName = formatter.formatCellValue(userNameCell).trim();
            Cell customerIdCell = row.getCell(customerIdColNum);
            String customerId = formatter.formatCellValue(customerIdCell).trim();
            String password = RandomUtils.generateRandomAlphanumericStr(12);
            Cell passwordCell = row.createCell(passwordColNum);
            passwordCell.setCellValue(password);
//            Cell passwordCell = row.getCell(passwordColNum);
//            String password = formatter.formatCellValue(passwordCell).trim();
            boolean repeated = false;
            if (totalUserNames.contains(userName)) {
//                log.debug("repeated customerName: {}", customerName);
                repeated = true;
            }
            totalUserNames.add(userName);
            if (!repeated) {
//                System.out.println("update examine.sys_dept set writing_generate_limit = 30 where dept_name='" + name + "';");

                /*
                1. yuqing customer
                if customerId exists in yuqing customer, skip
                 */
                Map<String, Object> queryForMap = null;
                try {
                    queryForMap = jdbcTemplate.queryForMap("select * from console_data.customer_info where id=" + customerId + ";");
                } catch (EmptyResultDataAccessException e) {
                    String insertCustomerInfoSql = "insert into console_data.customer_info " +
                            "(id, name, sys_name, sys_icon, user_type, end_date) values \n" +
                            "(" + customerId + ",'" + customerName + "', '舆情监测平台', " +
                            "'http://yuwoyg.oss-cn-hangzhou.aliyuncs.com/yuqing_project/sys_icon/770c8727-0120-4b18-bfac-a8f0b851e54e.png'," +
                            "0, '" + endDate + "');\n";
                    System.out.println(insertCustomerInfoSql);
                }
                /*
                2. Yuqing user
                If username exists in yuqing user, skip
                 */
                try {
                    queryForMap = jdbcTemplate.queryForMap("select * from console_data.sys_user where name='" + userName + "';");
                    System.out.println("SELECT @yuqingUserId := " + queryForMap.get("id") + ";\n");
                } catch (EmptyResultDataAccessException e) {
                    String insertYuqingUserSql = "insert into console_data.sys_user " +
                            "(name, pass, end_date, status, customer_id, group_id) values \n" +
                            "('" + userName + "', '" + HashUtils.md5(password).toUpperCase() + "', '" + endDate + "', 1, " + customerId + ", 149);\n";
                    System.out.println(insertYuqingUserSql);
                    String yuqingUserId = "SELECT @yuqingUserId := LAST_INSERT_ID();\n";
                    System.out.println(yuqingUserId);
                }
                /*
                3. Yuqing userRole
                 */
                String insertYuqingUserRoleSql = "insert into console_data.sys_user_role " +
                        "(user_id, role_id) values \n" +
                        "(@yuqingUserId, 7);\n";
                System.out.println(insertYuqingUserRoleSql);
                /*
                4. Examine dept
                If customerId exists in examine dept, skip
                 */
                try {
                    queryForMap = jdbcTemplate.queryForMap("select * from examine.sys_dept where customer_id=" + customerId + ";");
                    System.out.println("SELECT @deptId := " + queryForMap.get("dept_id") + ";\n");
                } catch (EmptyResultDataAccessException e) {

                    String insertDeptSql = "insert into examine.sys_dept " +
                            "(parent_id, ancestors, dept_name, examine_limit, transform_limit, customer_id, is_formal, " +
                            "end_date, writing_generate_limit) values \n" +
                            "(0, '0', '" + customerName + "', " + characterNum + ", " + docConvertNum + ", " + customerId + ", 0, '" + endDate + "' ," + writingNum + ");\n";
                    System.out.println(insertDeptSql);
                    String deptId = "SELECT @deptId := LAST_INSERT_ID();\n";
                    System.out.println(deptId);
                }
                // examine user
                /*
                5. Examine user
                If username exists in examine user, skip
                 */
                try {
                    queryForMap = jdbcTemplate.queryForMap("select * from examine.sys_user where user_name='" + userName + "';");
                    System.out.println("SELECT @examineUserId := " + queryForMap.get("user_id") + ";\n");
                } catch (EmptyResultDataAccessException e) {

                    String insertExamineUserSql = "insert into examine.sys_user " +
                            "(dept_id, user_name, nick_name, user_type, password, status, " +
                            "type, yuqing_user_id, yuqing_user_type, yuqing_group_id, end_date) values \n" +
                            "(@deptId, '" + userName + "', '" + userName + "', 1, '" + new BCryptPasswordEncoder().encode(HashUtils.md5(password).toUpperCase()) + "', 0, " +
                            "1, @yuqingUserId, 0, 149, '" + endDate + "');\n";
                    System.out.println(insertExamineUserSql);
                    String examineUserId = "SELECT @examineUserId := LAST_INSERT_ID();\n";
                    System.out.println(examineUserId);
                }
                String insertSysUserRoleSql = "insert into examine.sys_user_role (user_id, role_id) values \n" +
                        "(@examineUserId, 102);\n";
                System.out.println(insertSysUserRoleSql);
            }
        };
        String outputFile = excelModifier.modifyRows(inputFilePath, 1, rowsModifyConsumer);
        log.info("output file: {}", outputFile);
        System.out.println("totalCustomer size: " + totalUserNames.size());
//        getInsertCustomerBatchSql(insertNames);

        System.out.println("map size: " + map.size());
//        for (String key : map.keySet()) {
//            InsertUserInfo insertUserInfo = map.get(key);
//            generateYuqingExamineUser(Integer.valueOf(insertUserInfo.getCustomerId()),
//                    insertUserInfo.getCustomerName(),
//                    insertUserInfo.getUserName(),
//                    insertUserInfo.getPassword());
//
//        }
    }

    @Test
    @Disabled
    void AddUserToYuqingAndExamine() throws IOException {
        DataFormatter formatter = new DataFormatter();
        Set<String> totalNames = new LinkedHashSet<>();
        Set<String> insertNames = new LinkedHashSet<>();
        Map<String, InsertUserInfo> map = new LinkedHashMap<>();

        String endDate = "2023-06-30";
        Consumer<Row> rowsModifyConsumer = row -> {
            Cell userNameCell = row.getCell(0);
            String userName = formatter.formatCellValue(userNameCell).trim();
            Cell customerNameCell = row.getCell(1);
            String customerName = formatter.formatCellValue(customerNameCell).trim();
            Cell customerIdCell = row.getCell(2);
            String customerId = formatter.formatCellValue(customerIdCell).trim();
            String password = RandomUtils.generateRandomAlphanumericStr(12);
            boolean repeated = false;
            if (totalNames.contains(customerName)) {
//                log.debug("repeated customerName: {}", customerName);
                repeated = true;
            }
            totalNames.add(customerName);
            Map<String, Object> queryForMap = null;
            try {
                queryForMap = jdbcTemplate.queryForMap("select id, name from console_data.customer_info where id = " + customerId);
//                log.debug("yuqing customer exists: {}", customerName);
            } catch (EmptyResultDataAccessException e) {
                if (!repeated) {
                    // insert yuqing
                    String insertCustomerInfoSql = "insert into console_data.customer_info " +
                            "(id, name, sys_name, sys_icon, user_type, end_date) values \n" +
                            "(" + customerId + ",'" + customerName + "', '舆情监测平台', " +
                            "'http://yuwoyg.oss-cn-hangzhou.aliyuncs.com/yuqing_project/sys_icon/770c8727-0120-4b18-bfac-a8f0b851e54e.png'," +
                            "0, '" + endDate + "');\n";
                    System.out.println(insertCustomerInfoSql);
                }
            }
            try {
                queryForMap = jdbcTemplate.queryForMap("select * from console_data.sys_user where name = '" + userName + "'");
//                log.debug("yuqing user exists: {}", customerName);
                System.out.println("SELECT @yuqingUserId := " + queryForMap.get("id") + ";\n");
                System.out.println();
            } catch (EmptyResultDataAccessException e) {
                if (!repeated) {
                    String insertYuqingUserSql = "insert into console_data.sys_user " +
                            "(name, pass, end_date, status, customer_id, group_id) values \n" +
                            "('" + userName + "', '" + HashUtils.md5(password).toUpperCase() + "', '" + endDate + "', 1, " + customerId + ", 149);\n";
                    System.out.println(insertYuqingUserSql);
                    String yuqingUserId = "SELECT @yuqingUserId := LAST_INSERT_ID();\n";
                    System.out.println(yuqingUserId);
                    String insertYuqingUserRoleSql = "insert into console_data.sys_user_role " +
                            "(user_id, role_id) values \n" +
                            "(@yuqingUserId, 7);\n";
                    System.out.println(insertYuqingUserRoleSql);
                }
            }
            try {
                queryForMap = jdbcTemplate.queryForMap("select * from examine.sys_dept where customer_id = '" + customerId + "'");
                System.out.println("SELECT @deptId := " + queryForMap.get("dept_id") + ";\n");
//                log.debug("examine dept exists: {}", customerName);
            } catch (EmptyResultDataAccessException e) {
                if (!repeated) {
                    String insertDeptSql = "insert into examine.sys_dept " +
                            "(parent_id, ancestors, dept_name, examine_limit, transform_limit, customer_id, is_formal, " +
                            "end_date, writing_generate_limit) values \n" +
                            "(0, '0', '" + customerName + "', 200000, 30, " + customerId + ", 0, '" + endDate + "' ,0);\n";
                    System.out.println(insertDeptSql);
                    String deptId = "SELECT @deptId := LAST_INSERT_ID();\n";
                    System.out.println(deptId);
                }
            }
            try {
                queryForMap = jdbcTemplate.queryForMap("select * from examine.sys_user where user_name = '" + userName + "'");
//                log.debug("examine user exists: {}", userName);
            } catch (EmptyResultDataAccessException e) {
                if (!repeated) {
                    String insertExamineUserSql = "insert into examine.sys_user " +
                            "(dept_id, user_name, nick_name, user_type, password, status, " +
                            "type, yuqing_user_id, yuqing_user_type, yuqing_group_id, end_date) values \n" +
                            "(@deptId, '" + userName + "', '" + userName + "', 1, '" + new BCryptPasswordEncoder().encode(HashUtils.md5(password).toUpperCase()) + "', 0, " +
                            "1, @yuqingUserId, 0, 149, '" + endDate + "');\n";
                    System.out.println(insertExamineUserSql);
                    String examineUserId = "SELECT @examineUserId := LAST_INSERT_ID();\n";
                    System.out.println(examineUserId);
                    String insertSysUserRoleSql = "insert into examine.sys_user_role (user_id, role_id) values \n" +
                            "(@examineUserId, 2), (@examineUserId, 126);\n";
                    System.out.println(insertSysUserRoleSql);
                }
            }
//            if (!repeated) {
//                Cell cell = row.createCell(3);
//                cell.setCellValue(password);
//                log.info("no record for {}", customerName);
//                map.put(customerId, new InsertUserInfo(customerId, customerName, userName, password));
//            }
        };
        String outputFile = excelModifier.modifyRows("C:\\Users\\Taogen\\Desktop\\test_insert_users.xlsx", 0, rowsModifyConsumer);
        log.info("output file: {}", outputFile);
        System.out.println("totalCustomer size: " + totalNames.size());
//        getInsertCustomerBatchSql(insertNames);

        System.out.println("map size: " + map.size());
//        for (String key : map.keySet()) {
//            InsertUserInfo insertUserInfo = map.get(key);
//            generateYuqingExamineUser(Integer.valueOf(insertUserInfo.getCustomerId()),
//                    insertUserInfo.getCustomerName(),
//                    insertUserInfo.getUserName(),
//                    insertUserInfo.getPassword());
//
//        }
    }

    private Set<String> getNamesFromExcel(String filePath, Integer sheetNum, Integer startRow, Integer columnNum) throws IOException {
        Set<String> names = new LinkedHashSet<>();
        String fileSuffix = filePath.substring(filePath.lastIndexOf("."));
        try (
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));
                Workbook workbook = ExcelUtils.createWorkbookByFileSuffix(in, fileSuffix)
        ) {
            Sheet sheet = workbook.getSheetAt(sheetNum);
            Iterator<Row> iterator = sheet.iterator();
            Row row;
            if (startRow != null && startRow > 0) {
                for (int i = 0; i < startRow; i++) {
                    iterator.next();
                }
            }
            int totalUserNum = 0, existedUserNum = 0;
            while (iterator.hasNext()) {
                row = iterator.next();
                DataFormatter formatter = new DataFormatter();
                String name = formatter.formatCellValue(row.getCell(columnNum)).trim();
                if (StringUtils.isEmpty(name)) {
                    break;
                }
//                log.debug("name: {}", name);
                names.add(name);
                totalUserNum++;
            }
            log.debug("total user num: {}", totalUserNum);
        }
        log.debug("actual total user num: {}", names.size());
        return names;
    }

    private String getInsertCustomerBatchSql(Collection<String> names,
                                             String provinceCode,
                                             String cityCode,
                                             String countyCode,
                                             Integer sustainUserId,
                                             Integer saleUserId) {
        StringBuilder insertCustomerSql = new StringBuilder()
                .append("insert into crm_customer (name, type, is_formal, sale_user_id, " +
                        "sustain_user_id, province_code, city_code, county_code, user_id, remarks, product_type) " +
                        "values \n");
        List<String> itemList = new ArrayList<>();
        String itemTemplate = " ('%s', 52, 0, " + saleUserId + ", " + sustainUserId + ", " + provinceCode + "," + cityCode + "," + countyCode + ", -1, '批量开审核账号', '1') ";
        names.stream().forEach(item -> itemList.add(String.format(itemTemplate, item.trim())));
        insertCustomerSql.append(itemList.stream().collect(Collectors.joining("\n,")));
        System.out.println(insertCustomerSql);
        return insertCustomerSql.toString();
    }
}
