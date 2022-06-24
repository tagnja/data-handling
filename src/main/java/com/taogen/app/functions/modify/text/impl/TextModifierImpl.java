package com.taogen.app.functions.modify.text.impl;

import com.taogen.app.functions.modify.text.TextModifier;
import com.taogen.commons.datatypes.string.StringUtils;
import com.taogen.commons.io.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Taogen
 */
@Slf4j
@Component
public class TextModifierImpl implements TextModifier {
    @Override
    public String splitModifyAndJoin(String source, String splitStr, Function<String, String> itemModifyFunc, String joinStr) {
        String[] splitStrArray = source.split(splitStr);
        log.debug("split array: {}", Arrays.toString(splitStrArray));
        String result = Arrays.stream(splitStrArray)
                .map(itemModifyFunc)
                .collect(Collectors.joining(joinStr));
        log.info("result is: {}", result);
        return result;
    }

    @Override
    public String splitModifyAndJoinWithFile(String inputFilePath,
                                             String splitStr, Function<String, String> itemModifyFunc,
                                             String joinStr) throws FileNotFoundException {
        FileUtils.ensureFileExists(inputFilePath);
        String inputFileDir = FileUtils.getDirPathByFilePath(inputFilePath);
        log.debug("inputFileDir: {}", inputFileDir);
        String inputFileName = FileUtils.getFileNameByFilePath(inputFilePath);
        log.debug("inputFileName: {}", inputFileName);
        String outputFilePath = new StringBuilder()
                .append(inputFileDir)
                .append(File.separator)
                .append(FileUtils.appendDateTimeToFileName(inputFileName))
                .toString();
        try (BufferedReader bufferedReader = FileUtils.getBufferedReaderWithCharset(inputFilePath, StandardCharsets.UTF_8)) {
            StringBuilder stringBuilder = new StringBuilder();
            char[] buf = new char[1024];
            int len;
            while ((len = bufferedReader.read(buf)) != -1) {
                stringBuilder.append(buf, 0, len);
            }
            String toModifyString = stringBuilder.toString();
            toModifyString = StringUtils.removeNewLineCharacters(toModifyString);
            String handledText = splitModifyAndJoin(toModifyString,
                    splitStr, itemModifyFunc, joinStr);
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFilePath))) {
                bufferedWriter.write(handledText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("output file path: {}", outputFilePath);
        return outputFilePath;
    }
}
