package com.example.msm_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot Application for MSM AI Service (Persistence Layer).
 */
@SpringBootApplication(scanBasePackages = {"com.example.msm_ai", "com.example.library_helper"})
@EntityScan(basePackages = {"com.example.msm_ai.model"})
@EnableJpaRepositories(basePackages = {"com.example.msm_ai.repository"})
public class MsmAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsmAiApplication.class, args);
    }
}
