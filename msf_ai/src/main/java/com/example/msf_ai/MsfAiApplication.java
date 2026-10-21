package com.example.msf_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for MSF AI Service (Orchestration Layer).
 */
@SpringBootApplication(scanBasePackages = {"com.example.msf_ai", "com.example.library_helper"})
public class MsfAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsfAiApplication.class, args);
    }
}
