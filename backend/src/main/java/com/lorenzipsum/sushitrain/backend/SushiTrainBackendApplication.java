package com.lorenzipsum.sushitrain.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SushiTrainBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SushiTrainBackendApplication.class, args);
    }

}
