package com.scm.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EdgeKafkaStackConfigTest {

    @Test
    void edgeKafkaStackHasScriptsAndCiEntry() throws Exception {
        Path root = resolvePlatformRoot();
        String edgeKafkaCompose = read(root, "docker-compose.edge-kafka.yml");
        String edgeCompose = read(root, "docker-compose.edge.yml");
        String keycloakRealm = read(root, "deploy/keycloak/scm-realm.json");
        String startScript = read(root, "scripts/start-edge-kafka.sh");
        String stopScript = read(root, "scripts/stop-edge-kafka.sh");
        String runScript = read(root, "scripts/run-e2e-edge-kafka.sh");
        String omsDockerConfig = read(root, "scm-oms-service/src/main/resources/application-docker.yml");
        String workflow = read(root, ".github/workflows/scm-ci.yml");
        String rootWorkflow = read(root.getParent(), ".github/workflows/scm-platform-edge-kafka.yml");
        String kafkaOverlay = read(root, "docker-compose.kafka-overlay.yml");
        String compose = read(root, "docker-compose.yml");
        String parentPom = read(root, "pom.xml");
        String serviceDockerfile = read(root, "deploy/Dockerfile.service");
        String commonSpringImports = read(root,
                "scm-common-spring/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
        String jdbcStorageConfiguration = read(root,
                "scm-common-spring/src/main/java/com/scm/spring/storage/EnableJdbcStorageConfiguration.java");
        String erpApplication = read(root, "scm-erp-service/src/main/java/com/scm/erp/ErpApplication.java");
        String omsApplication = read(root, "scm-oms-service/src/main/java/com/scm/oms/OmsApplication.java");
        String wmsApplication = read(root, "scm-wms-service/src/main/java/com/scm/wms/WmsApplication.java");
        String tmsApplication = read(root, "scm-tms-service/src/main/java/com/scm/tms/TmsApplication.java");

        assertTrue(edgeKafkaCompose.contains("jdbc,docker,kafka,docker-kafka"));
        assertFalse(edgeKafkaCompose.contains("jwt,jwt-jwks,kafka,docker-kafka"));
        assertTrue(edgeKafkaCompose.contains("profiles: [\"legacy-gateway\"]"));
        assertUsesEdgeKafkaComposeChain(startScript);
        assertUsesEdgeKafkaComposeChain(stopScript);
        assertTrue(runScript.contains("-Pe2e-kafka"));
        assertTrue(runScript.contains("@E2E-K05"));
        assertTrue(runScript.contains("SCM_E2E_OMS_AUTH"));
        assertTrue(runScript.contains("SCM_E2E_OMS_AUTH:-none"));
        assertTrue(startScript.contains("SCM_COMPOSE_SKIP_BUILD"));
        assertTrue(startScript.contains("--no-build"));
        assertKafkaOverlayDoesNotUseEmptyProfiles(kafkaOverlay);
        assertOmsDockerUsesWmsInventory(omsDockerConfig);
        assertKafkaHasInternalAndExternalListeners(compose);
        assertEdgeStackAcceptsHostIssuedKeycloakTokens(edgeCompose);
        assertKeycloakRealmUsesClientRoles(keycloakRealm);
        assertServiceImagesUseCiFriendlyJvmDefaults(serviceDockerfile);
        assertServiceModulesBuildExecutableJars(parentPom);
        assertJdbcStorageIsAutoConfigured(commonSpringImports, jdbcStorageConfiguration,
                erpApplication, omsApplication, wmsApplication, tmsApplication);

        assertTrue(workflow.contains("e2e-edge-kafka-stack:"));
        assertTrue(workflow.contains("bash scripts/start-edge-kafka.sh"));
        assertTrue(workflow.contains("bash scripts/run-e2e-edge-kafka.sh"));
        assertTrue(workflow.contains("bash scripts/stop-edge-kafka.sh"));
        assertRootWorkflowTriggersScmWaveAndRunsK05(rootWorkflow);
    }

    private static void assertUsesEdgeKafkaComposeChain(String script) {
        assertTrue(script.contains("docker-compose.yml"));
        assertTrue(script.contains("docker-compose.full.yml"));
        assertTrue(script.contains("docker-compose.edge.yml"));
        assertTrue(script.contains("docker-compose.kafka-overlay.yml"));
        assertTrue(script.contains("docker-compose.edge-kafka.yml"));
    }

    private static void assertKafkaHasInternalAndExternalListeners(String compose) {
        assertTrue(compose.contains("confluentinc/cp-kafka:7.5.0"));
        assertTrue(compose.contains("9092:29092"));
        assertTrue(compose.contains("PLAINTEXT://kafka:9092"));
        assertTrue(compose.contains("EXTERNAL://localhost:9092"));
        assertTrue(compose.contains("CLUSTER_ID"));
        assertTrue(compose.contains("KAFKA_PROCESS_ROLES"));
        assertTrue(compose.contains("KAFKA_CONTROLLER_QUORUM_VOTERS"));
        assertTrue(compose.contains("KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT"));
        assertTrue(compose.contains("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR"));
        assertTrue(compose.contains("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR"));
        assertTrue(compose.contains("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR"));
    }

    private static void assertKafkaOverlayDoesNotUseEmptyProfiles(String kafkaOverlay) {
        assertFalse(kafkaOverlay.contains("profiles: []"));
    }

    private static void assertOmsDockerUsesWmsInventory(String omsDockerConfig) {
        assertTrue(omsDockerConfig.contains("provider: WMS"));
        assertTrue(omsDockerConfig.contains("wms-base-url: http://scm-wms:8082"));
    }

    private static void assertEdgeStackAcceptsHostIssuedKeycloakTokens(String edgeCompose) {
        assertTrue(edgeCompose.contains("SCM_JWT_ISSUER: http://localhost:8180/realms/scm"));
        assertTrue(edgeCompose.contains("SCM_JWT_JWKS_URI: http://keycloak:8080/realms/scm/protocol/openid-connect/certs"));
    }

    private static void assertKeycloakRealmUsesClientRoles(String keycloakRealm) {
        assertTrue(keycloakRealm.contains("\"defaultClientScopes\": [\"profile\", \"email\", \"roles\"]"));
        assertTrue(keycloakRealm.contains("\"scm-gateway\": [\"oms.write\", \"wms.write\"]"));
        assertFalse(keycloakRealm.contains("\"optionalClientScopes\": [\"oms.write\", \"wms.write\"]"));
    }

    private static void assertServiceImagesUseCiFriendlyJvmDefaults(String serviceDockerfile) {
        assertTrue(serviceDockerfile.contains("ENV JAVA_OPTS=\"-Xmx256m -Xms128m -XX:MaxMetaspaceSize=128m\""));
        assertFalse(serviceDockerfile.contains("-Xmx384m"));
    }

    private static void assertServiceModulesBuildExecutableJars(String parentPom) {
        assertTrue(parentPom.contains("<artifactId>spring-boot-maven-plugin</artifactId>"));
        assertTrue(parentPom.contains("<goal>repackage</goal>"));
    }

    private static void assertJdbcStorageIsAutoConfigured(String commonSpringImports, String jdbcStorageConfiguration,
                                                          String... serviceApplications) {
        assertTrue(commonSpringImports.contains("com.scm.spring.storage.EnableJdbcStorageConfiguration"));
        assertTrue(jdbcStorageConfiguration.contains("DataSource scmDataSource"));
        assertTrue(jdbcStorageConfiguration.contains("properties.initializeDataSourceBuilder().build()"));
        assertTrue(jdbcStorageConfiguration.contains("JdbcTemplate scmJdbcTemplate"));
        assertTrue(jdbcStorageConfiguration.contains("PlatformTransactionManager scmTransactionManager"));
        assertTrue(jdbcStorageConfiguration.contains("Flyway scmFlyway"));
        assertTrue(jdbcStorageConfiguration.contains("FlywayMigrationInitializer scmFlywayMigrationInitializer"));
        for (String serviceApplication : serviceApplications) {
            assertTrue(serviceApplication.contains("import com.scm.spring.storage.EnableJdbcStorageConfiguration;"));
            assertTrue(serviceApplication.contains("@Import(EnableJdbcStorageConfiguration.class)"));
        }
    }

    private static void assertRootWorkflowTriggersScmWaveAndRunsK05(String rootWorkflow) {
        assertTrue(rootWorkflow.contains("branches: [cursor/scm-wave, main, master]"));
        assertTrue(rootWorkflow.contains("working-directory: scm-platform"));
        assertTrue(rootWorkflow.contains("e2e-edge-kafka-stack:"));
        assertTrue(rootWorkflow.contains("Check Docker Compose availability"));
        assertTrue(rootWorkflow.contains("docker compose version"));
        assertTrue(rootWorkflow.contains("Validate edge + Kafka compose config"));
        assertTrue(rootWorkflow.contains("config --quiet"));
        assertTrue(rootWorkflow.contains("Build OpenResty gateway image"));
        assertTrue(rootWorkflow.contains("build scm-gateway-jwt"));
        assertTrue(rootWorkflow.contains("Build SCM service images"));
        assertTrue(rootWorkflow.contains("Start MySQL and Redis infrastructure"));
        assertTrue(rootWorkflow.contains("MySQL/Redis infrastructure start failed"));
        assertTrue(rootWorkflow.contains("logs --tail=120 mysql-erp mysql-oms mysql-wms mysql-tms redis"));
        assertTrue(rootWorkflow.contains("down -v --remove-orphans"));
        assertTrue(rootWorkflow.contains("up -d mysql-erp mysql-oms mysql-wms mysql-tms redis"));
        assertTrue(rootWorkflow.contains("Start Kafka infrastructure"));
        assertTrue(rootWorkflow.contains("up -d kafka"));
        assertTrue(rootWorkflow.contains("Wait for Kafka port"));
        assertTrue(rootWorkflow.contains("Start Keycloak infrastructure"));
        assertTrue(rootWorkflow.contains("up -d keycloak"));
        assertFalse(rootWorkflow.contains("Wait for Keycloak token endpoint"));
        assertFalse(rootWorkflow.contains("scope=openid%20oms.write"));
        assertTrue(rootWorkflow.contains("Start TMS service"));
        assertTrue(rootWorkflow.contains("up -d --no-build scm-tms"));
        assertTrue(rootWorkflow.contains("Wait for TMS port"));
        assertTrue(rootWorkflow.contains("curl -s --connect-timeout 2 http://127.0.0.1:8083/"));
        assertTrue(rootWorkflow.contains("ps -q --status exited scm-tms"));
        assertTrue(rootWorkflow.contains("logs --tail=200 scm-tms"));
        assertTrue(rootWorkflow.contains("Start ERP service"));
        assertTrue(rootWorkflow.contains("up -d --no-build scm-erp"));
        assertTrue(rootWorkflow.contains("Wait for ERP port"));
        assertTrue(rootWorkflow.contains("curl -s --connect-timeout 2 http://127.0.0.1:8084/"));
        assertTrue(rootWorkflow.contains("ps -q --status exited scm-erp"));
        assertTrue(rootWorkflow.contains("logs --tail=200 scm-erp"));
        assertTrue(rootWorkflow.contains("Start OMS and WMS services"));
        assertTrue(rootWorkflow.contains("up -d --no-build scm-oms scm-wms"));
        assertTrue(rootWorkflow.contains("Wait for OMS and WMS ports"));
        assertTrue(rootWorkflow.contains("curl -s --connect-timeout 2 http://127.0.0.1:8081/"));
        assertTrue(rootWorkflow.contains("curl -s --connect-timeout 2 http://127.0.0.1:8082/"));
        assertTrue(rootWorkflow.contains("logs --tail=200 scm-oms scm-wms"));
        assertFalse(rootWorkflow.contains("curl -sf http://127.0.0.1:808"));
        assertTrue(rootWorkflow.contains("Start mock services"));
        assertTrue(rootWorkflow.contains("up -d --no-build scm-mock-pay scm-mock-carrier scm-mock-inventory"));
        assertTrue(rootWorkflow.contains("Start JWT gateway service"));
        assertTrue(rootWorkflow.contains("up -d --no-build scm-gateway-jwt"));
        assertTrue(rootWorkflow.contains("bash scripts/run-e2e-edge-kafka.sh"));
        assertTrue(rootWorkflow.contains("Report E2E-K05 diagnostics"));
        assertTrue(rootWorkflow.contains("surefire-reports"));
        assertTrue(rootWorkflow.contains("::error file="));
        assertTrue(rootWorkflow.contains("bash scripts/stop-edge-kafka.sh"));
        assertTrue(rootWorkflow.contains("/dev/tcp/127.0.0.1/$p"));
        assertFalse(rootWorkflow.contains("continue-on-error: true"));
    }

    private static String read(Path root, String path) throws Exception {
        return Files.readString(root.resolve(path));
    }

    private static Path resolvePlatformRoot() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (cwd.getFileName().toString().equals("scm-contract-check")) {
            return cwd.getParent();
        }
        return cwd;
    }
}
