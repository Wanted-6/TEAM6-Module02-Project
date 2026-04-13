package com.wanted.projectmodule2lms.global.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.wanted.projectmodule2lms.domain")
@EntityScan(basePackages = "com.wanted.projectmodule2lms")
public class JpaConfig {
}