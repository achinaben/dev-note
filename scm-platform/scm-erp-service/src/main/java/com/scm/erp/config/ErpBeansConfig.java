package com.scm.erp.config;

import com.scm.common.idempotency.InMemoryProcessedMessageStore;
import com.scm.erp.integration.JdbcProcessedMessageStore;
import com.scm.common.idempotency.ProcessedMessageStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ErpBeansConfig {

    @Bean
    @ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
    ProcessedMessageStore inMemoryProcessedMessageStore() {
        return new InMemoryProcessedMessageStore();
    }

    @Bean
    @ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
    ProcessedMessageStore jdbcProcessedMessageStore(JdbcTemplate jdbc) {
        return new JdbcProcessedMessageStore(jdbc);
    }
}
