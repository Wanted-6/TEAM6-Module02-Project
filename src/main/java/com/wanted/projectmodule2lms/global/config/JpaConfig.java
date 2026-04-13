package com.wanted.projectmodule2lms.global.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.wanted.projectmodule2lms.domain")
<<<<<<< HEAD
@EntityScan(basePackages = "com.wanted.projectmodule2lms")
=======
@EntityScan(basePackages = "com.team6.project.domain")
>>>>>>> 3d9658988d8d980054b1097f00df51540345b299
public class JpaConfig {
}