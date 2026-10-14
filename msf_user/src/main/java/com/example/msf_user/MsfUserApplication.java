package com.example.msf_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for MSF User Service.
 */
@SpringBootApplication(scanBasePackages = {"com.example.msf_user", "com.example.library_helper"})
public class MsfUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsfUserApplication.class, args);
    }
}
