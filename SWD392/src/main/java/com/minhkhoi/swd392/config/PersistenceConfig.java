package com.minhkhoi.swd392.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.JpaRepository;
import com.minhkhoi.swd392.repository.RedisTokenRepository;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.minhkhoi.swd392.repository",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaRepository.class)
)
@EnableRedisRepositories(
    basePackages = "com.minhkhoi.swd392.repository",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RedisTokenRepository.class)
)
public class PersistenceConfig {
}
