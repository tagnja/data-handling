package com.taogen.datahandling.mysql.pattern;

import java.util.regex.Pattern;

/**
 * @author Taogen
 */
public class SqlQueryPatterns {
    /**
     * matched: select ... from
     * group 1
     * (?i) for case-insensitive
     */
    public static final String SELECT_SQL_REGEXP = "(?i)select((\\s|\\n|\\r\\n|\\t)+.+(\\s|\\n|\\r\\n|\\t)+)from(\\s|\\n|\\r\\n|\\t)+";
    public static final Pattern SELECT_SQL_PARSE_PATTERN = Pattern.compile(
            SELECT_SQL_REGEXP, Pattern.DOTALL);

    /**
     * matched: limit 10,100
     * group 2, 6
     * (?i) for case-insensitive
     */
    public static final String LIMIT_SQL_REGEXP = "(?i)limit(\\s|\\n|\\r\\n|\\t)+(\\d+)(\\s|\\n|\\r\\n|\\t)*(,)?(\\s|\\n|\\r\\n|\\t)*(\\d+)?(\\s|\\n|\\r\\n|\\t)*";
    public static final Pattern LIMIT_SQL_PARSE_PATTERN = Pattern.compile(
            LIMIT_SQL_REGEXP, Pattern.DOTALL);
}
