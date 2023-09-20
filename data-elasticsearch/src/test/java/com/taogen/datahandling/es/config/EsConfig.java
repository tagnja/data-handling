package com.taogen.datahandling.es.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

/**
 * @author taogen
 */
@Configuration
public class EsConfig extends ElasticsearchConfiguration {

    @Autowired
    private EsProperties esProperties;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(esProperties.getHost() + ":" + esProperties.getPort())
                .withBasicAuth(esProperties.getUsername(), esProperties.getPassword())
                .withConnectTimeout(esProperties.getConnectTimeout())
                .withSocketTimeout(esProperties.getSocketTimeout())
                .build();
    }
}
