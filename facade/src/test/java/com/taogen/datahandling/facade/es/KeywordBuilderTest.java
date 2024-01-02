package com.taogen.datahandling.facade.es;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author taogen
 */
class KeywordBuilderTest {
    @Test
    void getBoolQueryBuilderByExpression_should() {
        String keywordExpression = "a|b|c";
        BoolQueryBuilder boolQueryBuilder = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExpression);
        System.out.println(boolQueryBuilder);
        String expectResult = "{\n" +
                "  \"bool\" : {\n" +
                "    \"should\" : [\n" +
                "      {\n" +
                "        \"match_phrase\" : {\n" +
                "          \"title\" : {\n" +
                "            \"query\" : \"a\",\n" +
                "            \"slop\" : 0,\n" +
                "            \"zero_terms_query\" : \"NONE\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"match_phrase\" : {\n" +
                "          \"content\" : {\n" +
                "            \"query\" : \"a\",\n" +
                "            \"slop\" : 0,\n" +
                "            \"zero_terms_query\" : \"NONE\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"match_phrase\" : {\n" +
                "          \"title\" : {\n" +
                "            \"query\" : \"b\",\n" +
                "            \"slop\" : 0,\n" +
                "            \"zero_terms_query\" : \"NONE\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"match_phrase\" : {\n" +
                "          \"content\" : {\n" +
                "            \"query\" : \"b\",\n" +
                "            \"slop\" : 0,\n" +
                "            \"zero_terms_query\" : \"NONE\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"match_phrase\" : {\n" +
                "          \"title\" : {\n" +
                "            \"query\" : \"c\",\n" +
                "            \"slop\" : 0,\n" +
                "            \"zero_terms_query\" : \"NONE\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"match_phrase\" : {\n" +
                "          \"content\" : {\n" +
                "            \"query\" : \"c\",\n" +
                "            \"slop\" : 0,\n" +
                "            \"zero_terms_query\" : \"NONE\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"minimum_should_match\" : \"1\",\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";
        assertEquals(expectResult, boolQueryBuilder.toString());
    }

    @Test
    void getBoolQueryBuilderByExpression_must() {
        String keywordExpression = "a+b+c";
        BoolQueryBuilder boolQueryBuilderByExpression = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExpression);
        System.out.println(boolQueryBuilderByExpression);
        String expectResult = "{\n" +
                "  \"bool\" : {\n" +
                "    \"must\" : [\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"a\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"a\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"b\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"b\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"c\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"c\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";
        assertEquals(expectResult, boolQueryBuilderByExpression.toString());
    }

    @Test
    void getBoolQueryBuilderByExpression_mustnot() {
        String keywordExpression = "a-b-c";
        BoolQueryBuilder boolQueryBuilderByExpression = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExpression);
        System.out.println(boolQueryBuilderByExpression);
        String expect = "{\n" +
                "  \"bool\" : {\n" +
                "    \"must\" : [\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"a\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"a\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"must_not\" : [\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"b\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"b\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"c\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"c\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";
        assertEquals(expect, boolQueryBuilderByExpression.toString());
    }

    @Test
    void getBoolQueryBuilderByExpression_mix() {
        String keywordExpression = "(a+b-c)|(d|e)|(f-g)";
        BoolQueryBuilder boolQueryBuilderByExpression = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExpression);
        System.out.println(boolQueryBuilderByExpression);
        String expect = "{\n" +
                "  \"bool\" : {\n" +
                "    \"should\" : [\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"must\" : [\n" +
                "            {\n" +
                "              \"bool\" : {\n" +
                "                \"should\" : [\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"title\" : {\n" +
                "                        \"query\" : \"a\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"content\" : {\n" +
                "                        \"query\" : \"a\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                ],\n" +
                "                \"adjust_pure_negative\" : true,\n" +
                "                \"minimum_should_match\" : \"1\",\n" +
                "                \"boost\" : 1.0\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"bool\" : {\n" +
                "                \"should\" : [\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"title\" : {\n" +
                "                        \"query\" : \"b\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"content\" : {\n" +
                "                        \"query\" : \"b\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                ],\n" +
                "                \"adjust_pure_negative\" : true,\n" +
                "                \"minimum_should_match\" : \"1\",\n" +
                "                \"boost\" : 1.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"must_not\" : [\n" +
                "            {\n" +
                "              \"bool\" : {\n" +
                "                \"should\" : [\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"title\" : {\n" +
                "                        \"query\" : \"c\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"content\" : {\n" +
                "                        \"query\" : \"c\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                ],\n" +
                "                \"adjust_pure_negative\" : true,\n" +
                "                \"minimum_should_match\" : \"1\",\n" +
                "                \"boost\" : 1.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"d\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"d\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"e\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"e\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"must\" : [\n" +
                "            {\n" +
                "              \"bool\" : {\n" +
                "                \"should\" : [\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"title\" : {\n" +
                "                        \"query\" : \"f\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"content\" : {\n" +
                "                        \"query\" : \"f\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                ],\n" +
                "                \"adjust_pure_negative\" : true,\n" +
                "                \"minimum_should_match\" : \"1\",\n" +
                "                \"boost\" : 1.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"must_not\" : [\n" +
                "            {\n" +
                "              \"bool\" : {\n" +
                "                \"should\" : [\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"title\" : {\n" +
                "                        \"query\" : \"g\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"content\" : {\n" +
                "                        \"query\" : \"g\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                ],\n" +
                "                \"adjust_pure_negative\" : true,\n" +
                "                \"minimum_should_match\" : \"1\",\n" +
                "                \"boost\" : 1.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"minimum_should_match\" : \"1\",\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";
        assertEquals(expect, boolQueryBuilderByExpression.toString());
    }

    @Test
    void test() {
        String keywordExpression = "-f-g)"; // equals (a+b+(d|e))-c-(f-g)
        BoolQueryBuilder boolQueryBuilderByExpression = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExpression);
        System.out.println(boolQueryBuilderByExpression);

    }

    @Test
    void getBoolQueryBuilderByExpression_mix_2() {
        String keywordExpression = "(a+b-c)+(d|e)-(f-g)"; // equals (a+b+(d|e))-c-(f-g)
        BoolQueryBuilder boolQueryBuilderByExpression = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExpression);
        System.out.println(boolQueryBuilderByExpression);
        String expect = "{\n" +
                "  \"bool\" : {\n" +
                "    \"must\" : [\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"a\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"a\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"b\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"b\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"d\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"d\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"e\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"e\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"must_not\" : [\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"c\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"c\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"must\" : [\n" +
                "            {\n" +
                "              \"bool\" : {\n" +
                "                \"should\" : [\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"title\" : {\n" +
                "                        \"query\" : \"f\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"content\" : {\n" +
                "                        \"query\" : \"f\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                ],\n" +
                "                \"adjust_pure_negative\" : true,\n" +
                "                \"minimum_should_match\" : \"1\",\n" +
                "                \"boost\" : 1.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"must_not\" : [\n" +
                "            {\n" +
                "              \"bool\" : {\n" +
                "                \"should\" : [\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"title\" : {\n" +
                "                        \"query\" : \"g\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"match_phrase\" : {\n" +
                "                      \"content\" : {\n" +
                "                        \"query\" : \"g\",\n" +
                "                        \"slop\" : 0,\n" +
                "                        \"zero_terms_query\" : \"NONE\",\n" +
                "                        \"boost\" : 1.0\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                ],\n" +
                "                \"adjust_pure_negative\" : true,\n" +
                "                \"minimum_should_match\" : \"1\",\n" +
                "                \"boost\" : 1.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";
        assertEquals(expect, boolQueryBuilderByExpression.toString());
    }

    @Test
    void getBoolQueryBuilderByExpression_mix_3() {
        String keywordExpression = "((a|b)-c)+d+(e|f)-g-h"; // equals ((a|b)+d+(e|f))-c-g-h
        BoolQueryBuilder boolQueryBuilderByExpression = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExpression);
        System.out.println(boolQueryBuilderByExpression);
        String expect = "{\n" +
                "  \"bool\" : {\n" +
                "    \"must\" : [\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"a\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"a\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"b\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"b\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"d\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"d\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"e\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"e\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"f\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"f\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"must_not\" : [\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"c\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"c\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"g\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"g\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"title\" : {\n" +
                "                  \"query\" : \"h\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"match_phrase\" : {\n" +
                "                \"content\" : {\n" +
                "                  \"query\" : \"h\",\n" +
                "                  \"slop\" : 0,\n" +
                "                  \"zero_terms_query\" : \"NONE\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";
        assertEquals(expect, boolQueryBuilderByExpression.toString());
    }
}
