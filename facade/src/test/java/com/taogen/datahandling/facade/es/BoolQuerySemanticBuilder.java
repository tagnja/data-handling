package com.taogen.datahandling.facade.es;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;

/**
 * @author taogen
 */
public class BoolQuerySemanticBuilder {
    private BoolQueryBuilder boolQueryBuilder;

    public BoolQuerySemanticBuilder() {
        boolQueryBuilder = QueryBuilders.boolQuery();
    }

    public static BoolQueryBuilder getReplyBoolQueryBuilder() {
        BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
        BoolQueryBuilder condition1 = QueryBuilders.boolQuery();
        condition1.must(QueryBuilders.termQuery(EsField.SOURCE_ID, SourceType.WEIBO.getSourceId()));
        condition1.must(QueryBuilders.termQuery(EsField.IS_REPLY, "1"));
        BoolQueryBuilder condition2 = QueryBuilders.boolQuery();
        condition2.must(QueryBuilders.termQuery(EsField.SOURCE_ID, SourceType.SELF_MEDIA.getSourceId()));
        condition2.must(QueryBuilders.termQuery(EsField.IS_REPLY, "1"));
        BoolQueryBuilder condition3 = QueryBuilders.boolQuery();
        condition3.must(QueryBuilders.termQuery(EsField.RETWEETED_STATUS, "2"));
        BoolQueryBuilder condition4 = QueryBuilders.boolQuery();
        condition4.must(QueryBuilders.termQuery(EsField.REAL_SOURCE_ID, "20"));
        BoolQueryBuilder condition5 = QueryBuilders.boolQuery();
        condition5.must(QueryBuilders.termQuery(EsField.REAL_SOURCE_ID, "10"));
        boolFilter.should(condition1);
        boolFilter.should(condition2);
        boolFilter.should(condition3);
        boolFilter.should(condition4);
        boolFilter.should(condition5);
        return boolFilter;
    }

    public static List<String> getMediaTypeList(String[] typeArray) {
        ArrayList<String> typeList = new ArrayList<>();
        for (String type : typeArray) {
            switch (type) {
                case "1":
                    typeList.add("4");
                    break;
                case "2":
                    typeList.add("5");
                    break;
                case "3":
                    typeList.add("6");
                    break;
                case "7":
                    typeList.add("8");
                    break;
            }
            typeList.add(type);
        }
        return typeList;
    }

    public BoolQueryBuilder build() {
        return boolQueryBuilder;
    }

    public BoolQuerySemanticBuilder nonJunkData() {
        this.boolQueryBuilder.must(QueryBuilders.termQuery(EsField.STATUS, "0"));
        return this;
    }

    public BoolQuerySemanticBuilder keywordExp(String keywordExp) {
        BoolQueryBuilder keywordBoolQuery = KeywordBuilder.getBoolQueryBuilderByExpression(keywordExp);
        if (keywordBoolQuery != null) {
            boolQueryBuilder.must(keywordBoolQuery);
        }
        return this;
    }

    public BoolQuerySemanticBuilder dep(String depIds) {
        this.boolQueryBuilder.must(QueryBuilders.termsQuery(EsField.DEP, depIds.split(",")));
        return this;
    }

    public BoolQuerySemanticBuilder userFilter(String userId) {
        this.boolQueryBuilder.mustNot(QueryBuilders.termQuery(EsField.USER_TAG, new StringBuilder()
                .append("u_").append(userId).toString()));
        return this;
    }

    public BoolQuerySemanticBuilder source(List<String> sourceIds) {
        this.boolQueryBuilder.must(QueryBuilders.termsQuery(EsField.SOURCE_ID, sourceIds));
        return this;
    }

    public BoolQuerySemanticBuilder pubTimeRange(String startTime, String endTime) {
        this.boolQueryBuilder.must(QueryBuilders.rangeQuery(EsField.PUB_TIME)
                .gte(startTime)
                .lte(endTime));
        return this;
    }

    public BoolQuerySemanticBuilder addTimeRange(String startTime, String endTime) {
        this.boolQueryBuilder.must(QueryBuilders.rangeQuery(EsField.ADD_TIME)
                .gte(startTime)
                .lte(endTime));
        return this;
    }

    public BoolQuerySemanticBuilder deduplicate() {
        this.boolQueryBuilder.must(QueryBuilders.termQuery(EsField.IS_ORIGINAL, "1"));
        return this;
    }

    public BoolQuerySemanticBuilder sensitiveSys() {
        this.boolQueryBuilder.must(QueryBuilders.termQuery(EsField.LEVEL_ID, "1"));
        return this;
    }

    /**
     * 系统+用户敏感
     *
     * @param userId
     * @return
     */
    public BoolQuerySemanticBuilder sensitiveUser(String userId) {
        this.boolQueryBuilder.must(getSensitiveBoolQueryBuilder(userId));
        return this;
    }

    private QueryBuilder getSensitiveBoolQueryBuilder(String userId) {
        BoolQueryBuilder sensitiveBoolQueryBuilder = QueryBuilders.boolQuery();
        sensitiveBoolQueryBuilder.mustNot(QueryBuilders.termQuery(EsField.LEVEL_TAG, new StringBuilder()
                .append("u_").append(userId).append("_0").toString()));
        sensitiveBoolQueryBuilder.should(QueryBuilders.termQuery(EsField.LEVEL_ID, SensitiveType.SENSITIVE.getLevelId()));
        sensitiveBoolQueryBuilder.should(QueryBuilders.termQuery(EsField.LEVEL_TAG, new StringBuilder()
                .append("u_").append(userId).append("_1").toString()));
        return sensitiveBoolQueryBuilder;
    }

    /**
     * 系统非敏感
     *
     * @return
     */
    public BoolQuerySemanticBuilder nonSensitiveSys() {
        this.boolQueryBuilder.must(QueryBuilders.termQuery(EsField.LEVEL_ID, "-1"));
        return this;
    }

    /**
     * 系统+用户非敏感
     *
     * @param userId
     * @return
     */
    public BoolQuerySemanticBuilder nonSensitiveUser(String userId) {
        this.boolQueryBuilder.mustNot(getSensitiveBoolQueryBuilder(userId));
        return this;
    }

    /**
     * 已读
     *
     * @param userId
     * @return
     */
    public BoolQuerySemanticBuilder read(String userId) {
        this.boolQueryBuilder.must(QueryBuilders.termQuery(EsField.USER_TAG, new StringBuilder()
                .append("r_").append(userId).toString()));
        return this;
    }

    /**
     * 未读
     *
     * @param userId
     * @return
     */
    public BoolQuerySemanticBuilder nonRead(String userId) {
        this.boolQueryBuilder.mustNot(QueryBuilders.termQuery(EsField.USER_TAG, new StringBuilder()
                .append("r_").append(userId).toString()));
        return this;
    }

    /**
     * 站点
     * {"bool":{"should":[{"wildcard":{"source_url":"*.iesdouyin.com/*"}},{"wildcard":{"source_url":"*\//iesdouyin.com/*"}}],"minimum_should_match":1}}]}}
     *
     * @param host
     * @return
     */

    public BoolQuerySemanticBuilder host(String host) {
        BoolQueryBuilder hostFilter = QueryBuilders.boolQuery();
        hostFilter.should(QueryBuilders.wildcardQuery(EsField.SOURCE_URL, "*." + host + "/*"));
        hostFilter.should(QueryBuilders.wildcardQuery(EsField.SOURCE_URL, "*//" + host + "/*"));
        this.boolQueryBuilder.must(hostFilter);
        return this;
    }

    /**
     * IP 归属地
     * {"bool":{"should":[{"match_phrase":{"remark1":"\"ip_region\":[\"江西"}},{"match_phrase":{"remark1":"\"ip_region\":[\"中国\",\"江西"}}],"minimum_should_match":1}}]}
     *
     * @return
     */
    public BoolQuerySemanticBuilder ipArea(String area) {
        BoolQueryBuilder hostFilter = QueryBuilders.boolQuery();
        hostFilter.should(QueryBuilders.matchPhraseQuery(EsField.REMARK1, "\"ip_region\":[\"" + area));
        hostFilter.should(QueryBuilders.matchPhraseQuery(EsField.REMARK1, "\"ip_region\":[\"中国\",\"" + area));
        this.boolQueryBuilder.must(hostFilter);
        return this;
    }

    /**
     * 论坛-主贴
     *
     * @return
     */
    public BoolQuerySemanticBuilder forumPost() {
        BoolQueryBuilder forumboolQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder condition1 = QueryBuilders.boolQuery();
        condition1.must(QueryBuilders.termQuery(EsField.SOURCE_ID, SourceType.BBS.getSourceId()));
        condition1.must(QueryBuilders.termQuery(EsField.IS_REPLY, "0"));
        forumboolQueryBuilder.should(condition1);
        BoolQueryBuilder condition2 = QueryBuilders.boolQuery();
        condition2.must(QueryBuilders.termQuery(EsField.REAL_SOURCE_ID, "2"));
        forumboolQueryBuilder.should(condition2);
        BoolQueryBuilder condition3 = QueryBuilders.boolQuery();
        condition3.must(QueryBuilders.termQuery(EsField.REAL_SOURCE_ID, "19"));
        condition3.must(QueryBuilders.termQuery(EsField.RETWEETED_STATUS, "2"));
        forumboolQueryBuilder.mustNot(condition3);
        this.boolQueryBuilder.must(forumboolQueryBuilder);
        return this;
    }

    /**
     * 作者
     *
     * @param author
     * @return
     */
    public BoolQuerySemanticBuilder author(String author) {
        // term: exact query
        this.boolQueryBuilder.must(QueryBuilders.termQuery(EsField.AUTHOR, author));
        return this;
    }

    public BoolQuerySemanticBuilder authorId(String authorId) {
        // term: exact query
        this.boolQueryBuilder.must(QueryBuilders.termQuery(EsField.AUTHOR_ID, authorId));
        return this;
    }

    /**
     * 有图
     * {"bool":{"should":[{"term":{"img_json":"https"}},{"term":{"img_json":"http"}}],"minimum_should_match":1}}
     *
     * @return
     */
    public BoolQuerySemanticBuilder hasImg() {
        BoolQueryBuilder hasImgboolQueryBuilder = QueryBuilders.boolQuery();
        hasImgboolQueryBuilder.should(QueryBuilders.termQuery(EsField.IMG_JSON, "https"));
        hasImgboolQueryBuilder.should(QueryBuilders.termQuery(EsField.IMG_JSON, "http"));
        this.boolQueryBuilder.must(hasImgboolQueryBuilder);
        return this;
    }

    /**
     * 评论
     *
     * @return
     */
    public BoolQuerySemanticBuilder reply() {
        this.boolQueryBuilder.must(getReplyBoolQueryBuilder());
        return this;
    }

    /**
     * 正文
     *
     * @return
     */
    public BoolQuerySemanticBuilder notReply() {
        this.boolQueryBuilder.mustNot(getReplyBoolQueryBuilder());
        return this;
    }

    /**
     * 转发
     *
     * @return
     */
    public BoolQuerySemanticBuilder retweet() {
        this.boolQueryBuilder.must(QueryBuilders.termQuery(EsField.RETWEETED_STATUS, "1"));
        return this;
    }

    /**
     * 原创（非转发）
     *
     * @return
     */
    public BoolQuerySemanticBuilder notRetweet() {
        this.boolQueryBuilder.mustNot(QueryBuilders.termQuery(EsField.RETWEETED_STATUS, "1"));
        return this;
    }

    /**
     * 媒体级别
     *
     * @param mediaTypes 如：1,2,3
     * @return
     */
    public BoolQuerySemanticBuilder mediaType(String[] mediaTypes) {
        this.boolQueryBuilder.must(QueryBuilders.termsQuery(EsField.MEDIA_TYPE, getMediaTypeList(mediaTypes)));
        return this;
    }

    /**
     * 媒体省份
     *
     * @param province 如：江苏省
     * @return
     */
    public BoolQuerySemanticBuilder mediaLocation(String province) {
        this.boolQueryBuilder.must(QueryBuilders.termQuery(EsField.MEDIA_LOCATION, province));
        return this;
    }
}
