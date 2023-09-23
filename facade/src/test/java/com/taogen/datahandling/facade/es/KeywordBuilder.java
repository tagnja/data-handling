package com.taogen.datahandling.facade.es;

import com.taogen.commons.collection.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author taogen
 */
@Slf4j
public class KeywordBuilder {
    /**
     * Build BoolQueryBuilder by keyword expression
     * <p>
     * keyword: a-z, A-Z, 0-9, _, 中文
     * operator: |, +, - (All operators have the same priority)
     *
     * @param keywordExpression
     * @return
     */
    public static BoolQueryBuilder getBoolQueryBuilderByExpression(String keywordExpression) {
        System.out.println("keywordExpression: " + keywordExpression);
        if (keywordExpression == null || keywordExpression.trim().isEmpty()) {
            return null;
        }
        keywordExpression = keywordExpression.replaceAll("（", "(")
                .replaceAll("－", "-")
                .replaceAll("＋", "+")
                .replaceAll("）", ")")
                .replaceAll("｜", "|")
                .replaceAll(" ", "")
                .replaceAll("　", "");
        Pattern itemPattern = Pattern.compile(
                "([()|+-]|[0-9a-zA-Z\u4e00-\u9fa5]+)");
        Pattern operandPattern = Pattern.compile("[0-9a-zA-Z\u4e00-\u9fa5]+");
        Pattern symbolPattern = Pattern.compile("[()|+-]");
        Pattern operatorPattern = Pattern.compile("[|+-]");
        Queue<String> queue = new ArrayDeque<>();
        Deque<BoolQueryBuilder> operandStack = new ArrayDeque<>();
        Deque<String> symbolStack = new ArrayDeque<>();
        Matcher matcher = itemPattern.matcher(keywordExpression);
        while (matcher.find()) {
            queue.add(matcher.group(1));
        }
        System.out.println("queue: " + queue);
        while (!queue.isEmpty()) {
            String item = queue.poll();
            log.debug("item: {}", item);
            if (symbolPattern.matcher(item).matches()) {
                if (")".equals(item)) {
                    symbolStack.pop();
                    if (operandStack.size() > 1) {
                        String operator = symbolStack.pop();
                        BoolQueryBuilder operand2 = operandStack.pop();
                        BoolQueryBuilder operand1 = operandStack.pop();
                        operandStack.push(mergeBoolQueryBuilder(operand1, operand2, operator));
                    }
                } else {
                    symbolStack.push(item);
                }
            } else if (operandPattern.matcher(item).matches()) {
                BoolQueryBuilder keywordBoolFilter = getKeywordBoolFilter(item);
                if (!symbolStack.isEmpty() && operatorPattern.matcher(symbolStack.peek()).matches() &&
                        !operandStack.isEmpty()) {
                    String operator = symbolStack.pop();
                    operandStack.push(mergeBoolQueryBuilder(operandStack.pop(), keywordBoolFilter, operator));
                } else {
                    operandStack.push(keywordBoolFilter);
                }
            }
        }
        if (operandStack.size() != 1 || !symbolStack.isEmpty()) {
            throw new RuntimeException("表达式错误");
        }
        return operandStack.pop();
    }

    /**
     * Merge two BoolQueryBuilder by operator
     *
     * @param operand1
     * @param operand2
     * @param operator
     * @return
     */
    private static BoolQueryBuilder mergeBoolQueryBuilder(BoolQueryBuilder operand1, BoolQueryBuilder operand2, String operator) {
        if ("|".equals(operator)) {
            if (CollectionUtils.isEmpty(operand1.must()) &&
                    CollectionUtils.isEmpty(operand1.mustNot()) &&
                    CollectionUtils.isNotEmpty(operand1.should())) {
                // operand1 and operand2 only have should
                if (CollectionUtils.isNotEmpty(operand2.must()) ||
                        CollectionUtils.isNotEmpty(operand2.mustNot())) {
                    operand2 = QueryBuilders.boolQuery().should(operand2);
                }
                operand1.should().addAll(operand2.should());
                return operand1;
            } else {
                // operand1 has must or must_not
                return QueryBuilders.boolQuery()
                        .should(operand1)
                        .should(operand2)
                        .minimumShouldMatch(1);
            }
        } else if ("+".equals(operator)) {
            if (CollectionUtils.isEmpty(operand1.must()) && CollectionUtils.isEmpty(operand1.mustNot())) {
                // operand1 only have should
                operand1 = QueryBuilders.boolQuery().must(operand1);
            }
            operand1.must(operand2);
            return operand1;
        } else if ("-".equals(operator)) {
            if (CollectionUtils.isEmpty(operand1.must()) && CollectionUtils.isEmpty(operand1.mustNot())) {
                // operand1 only have should
                operand1 = QueryBuilders.boolQuery().must(operand1);
            }
            operand1.mustNot(operand2);
            return operand1;
        }
        return null;
    }

    private static BoolQueryBuilder getKeywordBoolFilter(String keyword) {
        MatchPhraseQueryBuilder titleQuery = QueryBuilders.matchPhraseQuery("title", keyword);
        MatchPhraseQueryBuilder contentQuery = QueryBuilders.matchPhraseQuery("content", keyword);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(titleQuery);
        boolQueryBuilder.should(contentQuery);
        boolQueryBuilder.minimumShouldMatch(1);
        return boolQueryBuilder;
    }

}
