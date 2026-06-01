package com.scm.spring.storage;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
@EnableConfigurationProperties({DataSourceProperties.class, FlywayProperties.class})
public class EnableJdbcStorageConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DataSource scmDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    Flyway scmFlyway(DataSource dataSource, FlywayProperties properties) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(properties.getLocations().toArray(String[]::new))
                .load();
    }

    @Bean
    @ConditionalOnBean(Flyway.class)
    @ConditionalOnMissingBean
    FlywayMigrationInitializer scmFlywayMigrationInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }
}
