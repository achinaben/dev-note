package com.scm.spring.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(IntegrationSecurityProperties.class)
public class IntegrationWebMvcConfiguration implements WebMvcConfigurer {
    private final IntegrationSecurityProperties properties;

    public IntegrationWebMvcConfiguration(IntegrationSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new IntegrationAccessInterceptor(properties))
                .addPathPatterns(
                        "/api/v1/integration/tms/**",
                        "/api/v1/integration/wms/**",
                        "/api/v1/integration/journal/**",
                        "/api/v1/integration/oms/**"
                );
    }
}
