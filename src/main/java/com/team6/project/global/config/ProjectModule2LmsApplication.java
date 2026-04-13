package com.team6.project.global.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.team6.project")
public class ProjectModule2LmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectModule2LmsApplication.class, args);
    }
}
