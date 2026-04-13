package com.team6.project.global.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.team6.project.domain")
@EntityScan(basePackages = "com.team6.project.domain")
public class JpaConfig {
}