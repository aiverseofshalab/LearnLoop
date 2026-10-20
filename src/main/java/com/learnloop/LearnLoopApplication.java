package com.learnloop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class LearnLoopApplication {

    public static void main(String[] args) {
        // Enforce UTC timezone for consistency across the entire application and JVM
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(LearnLoopApplication.class, args);
    }
}
