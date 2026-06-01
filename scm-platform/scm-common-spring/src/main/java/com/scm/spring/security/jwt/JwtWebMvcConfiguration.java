package com.scm.spring.security.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "scm.security.jwt", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(JwtSecurityProperties.class)
@Import(JwtCheckController.class)
public class JwtWebMvcConfiguration implements WebMvcConfigurer {

    private final JwtSecurityProperties properties;

    public JwtWebMvcConfiguration(JwtSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtAccessInterceptor(properties))
                .addPathPatterns("/api/v1/**");
    }
}
