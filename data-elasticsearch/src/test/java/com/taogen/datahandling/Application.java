package com.taogen.datahandling;

import com.taogen.datahandling.es.config.EsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author taogen
 */
@SpringBootApplication
@EnableConfigurationProperties(EsProperties.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
