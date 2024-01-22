package com.taogen.datahandling.office.excel.service.service.impl;

import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.office.excel.service.service.WordWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author taogen
 */
@Component
@Slf4j
public class WordWriterImpl implements WordWriter {
    public static void addPictureToDocument(XWPFDocument document, InputStream in, String fileName)
            throws IOException, InvalidFormatException {
        XWPFParagraph imageParagraph = document.createParagraph();
        imageParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun imageRun = imageParagraph.createRun();
        Integer textPosition = 100; // 1/2nd points
        imageRun.setTextPosition(textPosition);
        byte[] imageBytes = IOUtils.toByteArray(in);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        int imageWidth = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();
        double scalePercent = 1;
        int scaledWidth = (int) (imageWidth * scalePercent);
        int scaledHeight = (int) (imageHeight * scalePercent);
        imageRun.addPicture(new ByteArrayInputStream(imageBytes),
                XWPFDocument.PICTURE_TYPE_PNG,
                fileName,
                Units.toEMU(scaledWidth),
                Units.toEMU(scaledHeight));
    }

    @Override
    public String writeLabelAndDataToExcel(LabelAndData tableLabelAndData, String outputFilePath) throws IOException, InvalidFormatException {
        long startTime = System.currentTimeMillis();
        List<String> labels = tableLabelAndData.getLabels();
        List<List<Object>> valuesList = tableLabelAndData.getValuesList();
        try (XWPFDocument document = new XWPFDocument()) {
            for (int i = 0; i < valuesList.size(); i++) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(String.valueOf(i + 1));
                run.addBreak();
                for (int j = 0; j < labels.size() - 1; j++) {
                    String content = Objects.toString(valuesList.get(i).get(j)).replaceAll("(<br>|<p>|</p>|<article>|</article>|<div>|</div>)", "");
                    run.setText(new StringBuilder()
                            .append(labels.get(j))
                            .append(": ")
                            .append(content)
                            .toString());
                    run.addBreak();
                }
                run.setText(labels.get(labels.size() - 1) + ": ");
                // TODO
                String html = Objects.toString(valuesList.get(i).get(labels.size() - 1)).replaceAll("(<br>|<p>|</p>|<article>|</article>|<div>|</div>)", "");
                Pattern pattern = Pattern.compile(".*src=\"(.*?)\".*");
                Matcher matcher = pattern.matcher(html);
                if (matcher.matches()) {
                    Pattern pattern2 = Pattern.compile("src=\"(.*?)\"");
                    matcher = pattern2.matcher(html);
                    while (matcher.find()) {
                        System.out.println("groupCount: " + matcher.groupCount());
                        String imageUrl = matcher.group(1);
                        URLConnection connection = new URL(imageUrl).openConnection();
                        InputStream inputStream = connection.getInputStream();
                        log.debug("imageUrl: {}", imageUrl);
                        System.out.println("imageUrl: " + imageUrl);
                        addPictureToDocument(document, inputStream, imageUrl.substring(imageUrl.lastIndexOf("/") + 1));
                    }
                } else {
                    run.setText(html);
                    run.addBreak();
                }
            }
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFilePath))) {
                document.write(outputStream);
            }
        }
        log.info("write to word cost time: {} ms", System.currentTimeMillis() - startTime);
        return outputFilePath;
    }

}
