package com.example.msf_student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for MSF Student Service.
 */
@SpringBootApplication(scanBasePackages = {"com.example.msf_student", "com.example.library_helper"})
public class MsfStudentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsfStudentApplication.class, args);
    }
}
