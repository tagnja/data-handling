package com.taogen.datahandling.es.service;

import com.taogen.datahandling.common.vo.LabelAndData;
import com.taogen.datahandling.es.vo.DslQueryParam;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

/**
 * @author taogen
 */
public interface EsReader {

    /**
     * Read all data from elasticsearch
     * <p>
     * size in dsl is scroll fetch size. If size is null, it will use default size 10.
     * It's better to specify what fields you want to get in _source.
     *
     * @param restClient
     * @param dslQueryParam
     * @return
     * @throws IOException
     */
    LabelAndData readAll(RestClient restClient, DslQueryParam dslQueryParam) throws IOException;
}
