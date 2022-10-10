package com.taogen.app.functions.modify.text.impl;

import com.taogen.app.functions.modify.text.TextModifier;
import com.taogen.commons.io.DirectoryUtils;
import com.taogen.commons.io.FileUtils;
import com.taogen.commons.io.IOUtils;
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
    public String splitModifyAndJoin(String source, String splitDelimiter, Function<String, String> itemModifyFunc, String joinDelimiter) {
        String[] splitStrArray = source.split(splitDelimiter);
        log.debug("split array: {}", Arrays.toString(splitStrArray));
        String result = Arrays.stream(splitStrArray)
                .map(itemModifyFunc)
                .collect(Collectors.joining(joinDelimiter));
        log.debug("result is: {}", result);
        return result;
    }

    @Override
    public String splitModifyAndJoinWithFile(String inputFilePath,
                                             String splitDelimiter, Function<String, String> itemModifyFunc,
                                             String joinDelimiter) throws FileNotFoundException {
        if (!FileUtils.doesFileExist(inputFilePath)) {
            throw new RuntimeException("The input file does not exist");
        }
        String inputFileDir = DirectoryUtils.getDirPathByFile(new File(inputFilePath));
        log.debug("inputFileDir: {}", inputFileDir);
        String inputFileName = FileUtils.extractFileNameFromFilePath(inputFilePath);
        log.debug("inputFileName: {}", inputFileName);
        String outputFilePath = new StringBuilder()
                .append(inputFileDir)
                .append(File.separator)
                .append(FileUtils.appendDateTimeToFileName(inputFileName))
                .toString();
        try (BufferedReader bufferedReader = IOUtils.getBufferedReaderWithCharset(new FileInputStream(inputFilePath), StandardCharsets.UTF_8)) {
            StringBuilder stringBuilder = new StringBuilder();
            char[] buf = new char[1024];
            int len;
            while ((len = bufferedReader.read(buf)) != -1) {
                stringBuilder.append(buf, 0, len);
            }
            String toModifyString = stringBuilder.toString();
            toModifyString = IOUtils.removeNewLineCharacters(toModifyString);
            String handledText = splitModifyAndJoin(toModifyString,
                    splitDelimiter, itemModifyFunc, joinDelimiter);
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFilePath))) {
                bufferedWriter.write(handledText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("output file path: {}", outputFilePath);
        return outputFilePath;
    }

    @Override
    public String updateDelimiter(String source, String delimiter, String newDelimiter) {
        String[] splitStrArray = source.split(delimiter);
        log.debug("split array: {}", Arrays.toString(splitStrArray));
        String result = Arrays.stream(splitStrArray)
                .collect(Collectors.joining(newDelimiter));
        log.debug("result is: {}", result);
        return result;
    }
}
