package com.scm.spring.ops;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(OpsProperties.class)
public class OpsWebMvcConfiguration implements WebMvcConfigurer {
    private final OpsProperties opsProperties;

    public OpsWebMvcConfiguration(OpsProperties opsProperties) {
        this.opsProperties = opsProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OpsAccessInterceptor(opsProperties))
                .addPathPatterns("/api/v1/ops/**", "/wms/v1/ops/**", "/tms/v1/ops/**");
    }
}
