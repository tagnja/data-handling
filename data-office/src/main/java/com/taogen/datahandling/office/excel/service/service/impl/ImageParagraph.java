package com.taogen.datahandling.office.excel.service.service.impl;

import lombok.Builder;
import lombok.Data;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;

import java.io.InputStream;

/**
 * @author Taogen
 */
@Data
@Builder
public class ImageParagraph {
    private ParagraphAlignment paragraphAlignment = ParagraphAlignment.CENTER;

    /**
     * the amount by which text shall be raised or lowered for this picture
     */
    private Integer textPosition = 0;

    private InputStream inputStream;

    private String filename;

    /**
     * e.g. XWPFDocument.PICTURE_TYPE_PNG
     */
    private Integer pictureType;

    /**
     * unit: point
     */
    private Integer width;

    /**
     * unit: point
     */
    private Integer height;
}
