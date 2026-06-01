package com.scm.spring.web;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * OpenAPI / E2E 契约使用 snake_case JSON 字段名。
 */
@AutoConfiguration
public class ScmJacksonAutoConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer scmSnakeCaseJackson() {
        return builder -> builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
}
