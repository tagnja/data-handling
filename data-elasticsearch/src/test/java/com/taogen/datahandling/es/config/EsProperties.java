package com.taogen.datahandling.es.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author taogen
 */
@Configuration
@ConfigurationProperties(prefix = "es")
@Data
public class EsProperties {
    private String host;
    private int port;
    private String username;
    private String password;
    private int connectTimeout;
    private int socketTimeout;
}
