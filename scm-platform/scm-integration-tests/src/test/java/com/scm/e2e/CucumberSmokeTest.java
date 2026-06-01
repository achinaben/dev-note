package com.scm.e2e;

import org.junit.jupiter.api.Test;

/**
 * 默认 {@code mvn test} 不跑 Cucumber；完整 E2E 见 {@code RunCucumberE2E} + {@code -Pe2e}。
 */
class CucumberSmokeTest {
    @Test
    void e2eRunnerDocumented() {
        // E2E-01~10：scripts/start-all + mvn -pl scm-integration-tests -Pe2e test
    }
}
