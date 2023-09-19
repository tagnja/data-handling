package com.taogen.datahandling.office.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Taogen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Excel {
    public static final int DEFAULT_COLUMN_WIDTH = 16;

    public String name() default "";

    public double height() default 14;

    public double width() default 16;

    public String dateFormat() default "";

    public int sortNum() default Integer.MIN_VALUE;
}
