package com.wanted.projectmodule2lms.global.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wanted.projectmodule2lms")
public class ProjectModule2LmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectModule2LmsApplication.class, args);
    }
}
