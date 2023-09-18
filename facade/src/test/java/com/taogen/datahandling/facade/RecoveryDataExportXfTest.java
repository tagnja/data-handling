package com.taogen.datahandling.facade;

import com.taogen.commons.collection.CollectionUtils;
import com.taogen.commons.datatypes.string.StringUtils;
import com.taogen.commons.io.DirectoryUtils;
import com.taogen.commons.office.poi.ExcelUtils;
import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.facade.base.ExportBaseTest;
import com.taogen.datahandling.mysql.vo.SqlQueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Taogen
 */
@Slf4j
@Disabled
public class RecoveryDataExportXfTest extends ExportBaseTest {
    @BeforeEach
    void beforeEach() {
        showConfig();
    }

    private Integer xfResultCoNum = 8;
    private Integer errorLocationColNum = 9;
    private Integer mergedColNum = 9;
    private String SELF_ERROR_TYPE_PREFIX = "o_";
    private String SELF_ERROR_TYPE_PREFIX_2 = "c_";

    /**
     * 讯飞审核结果数据格式：
     * ["二级错误类型key"：[[错误位置, "错误词"，"修改建议"，"三级错误类型key/name"],...], ...]
     * 我们把多个二级错误类型合并而一个一级错误类型，实际审核结果中没有一级错误类型。
     * 解析过程：
     * 一个错误项中有二级错误类型key和三级错误类型key/name。三级错误类型可能为空字符串。
     * 通过二级错误类型key，找到一级类型名称。三级错误类型进行翻译或者直接显示名称。
     *
     * @throws IOException
     */
    @Test
    void exportRecoveryData() throws IOException {
        Long startTime = System.currentTimeMillis();
        String sql = "select rd.id as \"ID\", rd.title as \"标题\", " +
                "rd.content as \"内容\", " +
                "rd.pubtime as \"发布时间\", rd.gmt_create as \"入库时间\" ,rd.url as \"链接\", rg.name as \"客户组\", rs.name as \"站点名称\", xf_result, remark as \"错误定位\"\n" +
                "from app_wzjc.recovery_data rd \n" +
                "left join recovery_group rg on rg.id = rd.group_id \n" +
                "left join recovery_site rs on rs.id = rd.site_id\n" +
                "where group_id = 314 and words_audited_xf=1 order by rd.pubtime desc ";
//                + "limit 100";
//                "where remark is not null order by rd.id desc limit 100";
        SqlQueryParam sqlQueryParam = new SqlQueryParam();
        sqlQueryParam.setSql(sql);
        sqlQueryParam.setBatchFetch(false);
        LabelAndData tableLabelsAndData = mysqlReader.read(jdbcTemplate, sqlQueryParam);
        String outputDirPath = DirectoryUtils.getUserHomeDir() + File.separator + "export";
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists() || !outputDir.isDirectory()) {
            outputDir.mkdirs();
        }
        String outputFileName = new StringBuilder()
                .append("审核-数据-")
                .append(System.currentTimeMillis())
                .append(".xlsx")
                .toString();
        String outputFilePath = new StringBuilder()
                .append(outputDirPath)
                .append(File.separator)
                .append(outputFileName)
                .toString();
//        String outputFilePath = mysql2ExcelConverter.writeLabelAndDataToExcel(tableLabelsAndData,
//                outputDir, outputFileName);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            XSSFCellStyle titleCellStyle = createTitleCellStyle(sheet.getWorkbook());
            int rowNum = 0;
            List<String> labels = tableLabelsAndData.getLabels();
            labels.addAll(Arrays.asList("错误词", "修改建议", "错误类型", "二级错误类型", "三级错误类型"));
            XSSFRow titleRow = writeTitle(sheet, rowNum, labels, titleCellStyle);
            titleRow.setHeight((short) (20 * 20));
            rowNum++;
            writeData(sheet, rowNum, tableLabelsAndData.getValuesList());
            try (BufferedOutputStream outputStream = new BufferedOutputStream(
                    new FileOutputStream(outputFilePath))) {
                workbook.write(outputStream);
            }
        }
        log.info("output file path: {}", outputFilePath);
        log.info("Elapsed time: {} ms", System.currentTimeMillis() - startTime);
    }

    @Test
    @Disabled
    void removeRows() throws IOException {
        String outputDir = DirectoryUtils.getUserHomeDir() + File.separator + "export";
        String outputFileName = "审核-数据-1666576500831.xlsx";
        String inputFilePath = new StringBuilder()
                .append(outputDir)
                .append(File.separator)
                .append(outputFileName)
                .toString();
        String outputFilePath = excelModifier.removeRows(inputFilePath, row -> {
            Cell cell = row.getCell(xfResultCoNum);
            if (cell != null) {
                String stringCellValue = cell.getStringCellValue();
                log.debug("cell value: {}", stringCellValue);
                return StringUtils.isEmpty(stringCellValue);
            }
            return true;
        });
        log.info("output file path: {}", outputFilePath);
    }

    private void writeData(XSSFSheet sheet, int rowNum, List<List<Object>> valuesList) {
//       XSSFCellStyle xssfCellStyle = createContentCellStyle(sheet.getWorkbook());
        for (int i = 0; i < valuesList.size(); i++) {
            List<Object> values = valuesList.get(i);
            String xfResult = Objects.toString(values.get(xfResultCoNum));
            List<ErrorItem> errorInfoItems = getErrorInfoItemsByJsonStr(xfResult);
            // filter no error word items
            if (CollectionUtils.isEmpty(errorInfoItems)) {
                continue;
            }
            int startRowNum = rowNum;
            XSSFRow row = sheet.createRow(rowNum++);
            int generatedRowNum = 0;
            int colNum = 0;
            CellStyle linkCellStyle = ExcelUtils.createLinkCellStyle(sheet.getWorkbook());
            for (; colNum < values.size(); colNum++) {
                XSSFCell cell = row.createCell(colNum);
                ExcelUtils.setCellValueByObject(cell, values.get(colNum));
                String cellValue = Objects.toString(values.get(colNum));
//                cell.setCellStyle(xssfCellStyle);
                if (colNum == errorLocationColNum && StringUtils.isNotEmpty(cellValue) &&
                        cellValue.startsWith("http")) {
                    cell.setHyperlink(ExcelUtils.createHyperLinkByUrl(sheet.getWorkbook(), String.valueOf(values.get(colNum))));
                    cell.setCellStyle(linkCellStyle);
                }
            }
            for (ErrorItem errorItem : errorInfoItems) {
                XSSFCell cell = row.createCell(colNum);
                ExcelUtils.setCellValueByObject(cell, errorItem.getErrorWord());
                colNum++;
                cell = row.createCell(colNum);
                ExcelUtils.setCellValueByObject(cell, errorItem.getRightWord());
                colNum++;
                cell = row.createCell(colNum);
                ExcelUtils.setCellValueByObject(cell, errorItem.getErrorType());
                colNum++;
                cell = row.createCell(colNum);
                ExcelUtils.setCellValueByObject(cell, errorItem.getSecondErrorType());
                colNum++;
                cell = row.createCell(colNum);
                ExcelUtils.setCellValueByObject(cell, errorItem.getThirdErrorType());
                colNum++;
                colNum = values.size();
                row = sheet.createRow(rowNum++);
                generatedRowNum++;
            }
            if (generatedRowNum > 1) {
                for (int j = 0; j <= mergedColNum; j++) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + generatedRowNum - 1, j, j));
                    Cell mergedCell = CellUtil.getCell(CellUtil.getRow(startRowNum, sheet), j);
//                    mergedCell.setCellStyle(createContentCellStyle(sheet.getWorkbook()));
                    CellUtil.setVerticalAlignment(mergedCell, VerticalAlignment.CENTER);
                    CellUtil.setAlignment(mergedCell, HorizontalAlignment.LEFT);
                }
            }
            if (errorInfoItems.size() > 0) {
                rowNum--;
            }
        }
    }

    private XSSFCellStyle createContentCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
//        cellStyle.setAlignment(HorizontalAlignment.CENTER);
//        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//        cellStyle.setWrapText(true);
        // backgroundColor: 221, 235, 247
        // borderColor: 155, 194, 230
//        byte[] rgb = {(byte) 149, (byte) 179, (byte) 215};
        byte[] backgroundRgb = {(byte) 221, (byte) 235, (byte) 247};
        byte[] borderRgb = {(byte) 155, (byte) 194, (byte) 230};
        XSSFColor borderXssfColor = new XSSFColor(borderRgb, new DefaultIndexedColorMap());
//        cellStyle.setFillForegroundColor(new XSSFColor(backgroundRgb, new DefaultIndexedColorMap()));
//        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setBorderTop(BorderStyle.THICK);
        cellStyle.setTopBorderColor(borderXssfColor);
        cellStyle.setBorderBottom(BorderStyle.THICK);
        cellStyle.setBottomBorderColor(borderXssfColor);
        cellStyle.setBorderLeft(BorderStyle.THICK);
        cellStyle.setLeftBorderColor(borderXssfColor);
        cellStyle.setBorderRight(BorderStyle.THICK);
        cellStyle.setRightBorderColor(borderXssfColor);
        XSSFFont xssfFont = new XSSFFont();
        xssfFont.setColor(IndexedColors.BLUE.getIndex());
        xssfFont.setBold(true);
        cellStyle.setFont(xssfFont);
        return cellStyle;
    }

    private XSSFRow writeTitle(XSSFSheet sheet, int rowNum, List<String> labels, XSSFCellStyle titleCellStyle) {
        XSSFRow row = sheet.createRow(rowNum);
        int colNum = 0;
        for (; colNum < labels.size(); colNum++) {
            XSSFCell cell = row.createCell(colNum);
            cell.setCellStyle(titleCellStyle);
            ExcelUtils.setCellValueByObject(cell, labels.get(colNum));
        }
        return row;
    }

    @Data
    @AllArgsConstructor
    static class ErrorItem {
        private String errorWord;
        private String rightWord;
        private String errorType;
        private String secondErrorType;
        private String thirdErrorType;
    }

    @Test
    void handleRecoveryData() throws IOException {
        // start to call modifyWorkbook
        DataFormatter formatter = new DataFormatter();
        Consumer<Row> rowsModifyConsumer = row -> {
            String xfResult = row.getCell(8).getStringCellValue();
            if (!"xf_result".equals(xfResult)) {
                List<ErrorItem> errorItems = getErrorInfoItemsByJsonStr(xfResult);
                Map<String, List<ErrorItem>> errorResultMap = errorItems.stream().collect(Collectors.groupingBy(ErrorItem::getErrorType));
                log.debug("errorResultMap is \n{}", errorResultMap);
                int appendStartCol = 9;
                for (Map.Entry<String, List<ErrorItem>> entry : errorResultMap.entrySet()) {
                    Cell cell = row.createCell(appendStartCol++);
                    cell.setCellValue(entry.getValue().stream().map(item -> item.getErrorType() + "：" + item.getErrorWord() + "(" + item.getRightWord() + ")").collect(Collectors.joining("、")));
                }
            }
        };
        String outputFilePath = excelModifier.modifyRows("C:\\Users\\Taogen\\Desktop\\export\\审核-数据-1666339953293.xlsx", 0, rowsModifyConsumer);
        log.info(outputFilePath);
    }

    Map<String, List<String>> errorTypesMap = null;

    Map<String, String> secondLevelErrorTypeMap = null;

    Map<String, String> thirdLevelErrorTypeMap = null;

    private Map<String, String> getThirdLevelErrorTypeMap() {
        if (thirdLevelErrorTypeMap != null) {
            return thirdLevelErrorTypeMap;
        }
        thirdLevelErrorTypeMap = new LinkedHashMap<>();
        thirdLevelErrorTypeMap.put("mat_C", "句式杂糅");
        thirdLevelErrorTypeMap.put("repeat_P", "语意重复");
        thirdLevelErrorTypeMap.put("org_N", "名称变更");
        thirdLevelErrorTypeMap.put("org_M", "名称缺失");
        thirdLevelErrorTypeMap.put("org_R", "名称冗余");
        thirdLevelErrorTypeMap.put("org_S", "错别字");
        thirdLevelErrorTypeMap.put("org_P", "多种错误");
        thirdLevelErrorTypeMap.put("Ns", "地名错误");
        thirdLevelErrorTypeMap.put("Nh", "人名错误");
        thirdLevelErrorTypeMap.put("date-d", "日期错误");
        thirdLevelErrorTypeMap.put("date-m", "月份错误");
        thirdLevelErrorTypeMap.put("time", "时间错误");
        return thirdLevelErrorTypeMap;
    }

    private Map<String, String> getSecondLevelErrorTypeMap() {
        if (secondLevelErrorTypeMap != null) {
            return secondLevelErrorTypeMap;
        }
        secondLevelErrorTypeMap = new LinkedHashMap<>();
        secondLevelErrorTypeMap.put("char", "别字纠错");
        secondLevelErrorTypeMap.put("word", "别词纠错");
        secondLevelErrorTypeMap.put("grammar_m", "缺失");
        secondLevelErrorTypeMap.put("grammar_r", "冗余");
        secondLevelErrorTypeMap.put("grammar_pc", "句式杂糅");
        secondLevelErrorTypeMap.put("lx_char", "乱序_字");
        secondLevelErrorTypeMap.put("lx", "乱序_词");
        secondLevelErrorTypeMap.put("idm", "成语");
        secondLevelErrorTypeMap.put("collocation", "搭配");
        secondLevelErrorTypeMap.put("meta", "元宇宙");
        secondLevelErrorTypeMap.put("org", "实体纠错");
        secondLevelErrorTypeMap.put("number", "数字");
        secondLevelErrorTypeMap.put("punc", "标点");
        secondLevelErrorTypeMap.put("name_o", "人名顺序");
        secondLevelErrorTypeMap.put("title_s", "职务搭配及职务顺序");
        secondLevelErrorTypeMap.put("name_s", "副国级人名");
        secondLevelErrorTypeMap.put("quote_s", "领导人语录");
        secondLevelErrorTypeMap.put("pol", "政治用语");
        secondLevelErrorTypeMap.put("nat_p", "民族");
        secondLevelErrorTypeMap.put("dom_p", "主权");
        secondLevelErrorTypeMap.put("reg_p", "港澳台");
        secondLevelErrorTypeMap.put("gov_O", "机关顺序");
        secondLevelErrorTypeMap.put("intl", "国际关系");
        secondLevelErrorTypeMap.put("meeting", "会议");
        secondLevelErrorTypeMap.put("rude", "不文明用语");
        secondLevelErrorTypeMap.put("blackList", "黑名单");
        secondLevelErrorTypeMap.put("cs_correction", "英文拼写");
        secondLevelErrorTypeMap.putAll(secondLevelErrorTypeMap.entrySet().stream()
                .collect(Collectors.toMap(item -> SELF_ERROR_TYPE_PREFIX + item.getKey(), item -> item.getValue())));
        secondLevelErrorTypeMap.put(SELF_ERROR_TYPE_PREFIX_2 + "name_o", "人名顺序");
        return secondLevelErrorTypeMap;

    }

    private Map<String, List<String>> getXfErrorTypesMap() {
        if (errorTypesMap != null) {
            return errorTypesMap;
        }
        List<String> grammarErrorTypes = new ArrayList<>(Arrays.asList(
                "char", "word", "grammar_m", "grammar_r", "grammar_pc", "lx_char", "lx", "idm", "collocation", "meta"));
        grammarErrorTypes.addAll(grammarErrorTypes.stream().map(item -> SELF_ERROR_TYPE_PREFIX + item).collect(Collectors.toList()));
        List<String> objectErrorTypes = new ArrayList<>(Arrays.asList("org"));
        objectErrorTypes.addAll(objectErrorTypes.stream().map(item -> SELF_ERROR_TYPE_PREFIX + item).collect(Collectors.toList()));
        List<String> dateTimeErrorTypes = new ArrayList<>(Arrays.asList("number"));
        dateTimeErrorTypes.addAll(dateTimeErrorTypes.stream().map(item -> SELF_ERROR_TYPE_PREFIX + item).collect(Collectors.toList()));
        List<String> pucErrorTypes = new ArrayList<>(Arrays.asList("punc"));
        List<String> leaderSortErrorTypes = new ArrayList<>(Arrays.asList("name_o", "title_s", "name_s"));
        leaderSortErrorTypes.add(SELF_ERROR_TYPE_PREFIX_2 + "name_o");
        leaderSortErrorTypes.addAll(leaderSortErrorTypes.stream().map(item -> SELF_ERROR_TYPE_PREFIX + item).collect(Collectors.toList()));
        List<String> ideologyErrorTypes = new ArrayList<>(Arrays.asList(
                "quote_s", "pol", "nat_p", "dom_p", "reg_p", "gov_O", "intl", "meeting", "rude", "blackList"));
        ideologyErrorTypes.addAll(ideologyErrorTypes.stream().map(item -> SELF_ERROR_TYPE_PREFIX + item).collect(Collectors.toList()));
        List<String> englishErrorTypes = new ArrayList<>(Arrays.asList("cs_correction"));
        englishErrorTypes.addAll(englishErrorTypes.stream().map(item -> SELF_ERROR_TYPE_PREFIX + item).collect(Collectors.toList()));

        errorTypesMap = new LinkedHashMap<>();
        errorTypesMap.put("语句表述错误", grammarErrorTypes);
        errorTypesMap.put("实体名称错误", objectErrorTypes);
        errorTypesMap.put("日期错误", dateTimeErrorTypes);
//        errorTypesMap.put("标点错误", pucErrorTypes);
        errorTypesMap.put("领导人及排序错误", leaderSortErrorTypes);
        errorTypesMap.put("意识形态表述错误", ideologyErrorTypes);
        errorTypesMap.put("英文错误", englishErrorTypes);
        errorTypesMap.put("政策文件", new ArrayList<>(Arrays.asList("o_pol_file")));
        errorTypesMap.put("其他意识形态相关", new ArrayList<>(Arrays.asList("o_pol_other")));
        return errorTypesMap;
    }

    private List<ErrorItem> getErrorInfoItemsByJsonStr(String xfResult) {
        if (StringUtils.isEmpty(xfResult) || "null".equals(xfResult)) {
            return Collections.emptyList();
        }
        log.debug("xfResult is {}", xfResult);
        List<ErrorItem> errorItems = new ArrayList<>();
        JSONObject jsonObject = null;
        Set<String> notExistKeySet = new HashSet<>();
        try {
            jsonObject = new JSONObject(xfResult);
            for (String secondLevelErrorTypeKey : jsonObject.keySet()) {
                boolean keyExist = false;
                for (Map.Entry<String, List<String>> entry : getXfErrorTypesMap().entrySet()) {
                    for (String secondLevelErrorTypeKeyInMap : entry.getValue()) {
                        if (secondLevelErrorTypeKeyInMap.equals(secondLevelErrorTypeKey)) {
                            keyExist = true;
                            JSONArray errorInfoItems = jsonObject.getJSONArray(secondLevelErrorTypeKey);
                            if (errorInfoItems.length() == 0) {
                                continue;
                            }
                            for (int i = 0; i < errorInfoItems.length(); i++) {
                                JSONArray errorInfo = errorInfoItems.getJSONArray(i);
                                if (errorInfo.length() == 0) {
                                    log.info("key {} item is null", secondLevelErrorTypeKey);
                                    continue;
                                }
                                String suggestion = errorInfo.getString(2);
                                if (StringUtils.isEmpty(suggestion))
                                    suggestion = "<无建议>";
                                String thirdLevelErrorType = null;
                                try {
                                    thirdLevelErrorType = errorInfo.getString(3);
                                } catch (JSONException e) {
                                    // don't need to handle the exception
                                }
                                String thirdErrorTypeName = getThirdLevelErrorTypeMap().get(thirdLevelErrorType);
                                if (thirdErrorTypeName != null) {
                                    thirdLevelErrorType = thirdErrorTypeName;
                                }
                                errorItems.add(new ErrorItem(
                                        errorInfo.getString(1),
                                        suggestion,
                                        entry.getKey(),
                                        getSecondLevelErrorTypeMap().get(secondLevelErrorTypeKeyInMap),
                                        thirdLevelErrorType
                                ));
                            }
                        }
                    }
                }
                if (!keyExist) {
                    notExistKeySet.add(secondLevelErrorTypeKey);
                }
            }
            log.error("Not exist key is : {}", notExistKeySet);
        } catch (JSONException e) {
            log.info(xfResult);
            throw new RuntimeException(e);
        }
        return errorItems;
    }
}
