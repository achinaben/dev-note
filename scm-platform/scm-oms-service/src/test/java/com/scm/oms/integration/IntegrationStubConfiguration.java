package com.scm.oms.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class IntegrationStubConfiguration {

    @Bean
    @Primary
    WmsGateway stubWmsGateway() {
        return new StubWmsGateway();
    }
}
