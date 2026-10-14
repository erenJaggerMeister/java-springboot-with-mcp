package com.example.msf_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for MSF Auth Service.
 */
@SpringBootApplication(scanBasePackages = {"com.example.msf_auth", "com.example.library_helper"})
public class MsfAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsfAuthApplication.class, args);
    }
}
