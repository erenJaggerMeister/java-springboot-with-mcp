package com.example.msm_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot Application for MSM User Service.
 */
@SpringBootApplication(scanBasePackages = {"com.example.msm_user", "com.example.library_helper"})
@EntityScan(basePackages = {"com.example.msm_user.model"})
@EnableJpaRepositories(basePackages = {"com.example.msm_user.repository"})
public class MsmUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsmUserApplication.class, args);
    }
}
