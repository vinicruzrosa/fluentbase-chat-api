package com.fluency.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
@EnableReactiveMongoAuditing
public class FluencyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FluencyApiApplication.class, args);
    }
}
