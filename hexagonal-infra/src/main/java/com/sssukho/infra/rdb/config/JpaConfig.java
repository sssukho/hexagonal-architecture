package com.sssukho.infra.rdb.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"com.sssukho.infra.rdb.repository"})
@EntityScan(basePackages = {"com.sssukho.infra.rdb.entity"})
public class JpaConfig {

}

