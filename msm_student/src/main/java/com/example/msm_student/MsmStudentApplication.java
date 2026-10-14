package com.example.msm_student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot Application for MSM Student Service.
 */
@SpringBootApplication(scanBasePackages = {"com.example.msm_student", "com.example.library_helper"})
@EntityScan(basePackages = {"com.example.msm_student.model"})
@EnableJpaRepositories(basePackages = {"com.example.msm_student.repository"})
public class MsmStudentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsmStudentApplication.class, args);
    }
}
