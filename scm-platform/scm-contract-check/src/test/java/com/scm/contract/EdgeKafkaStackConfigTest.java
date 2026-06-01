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
        String startScript = read(root, "scripts/start-edge-kafka.sh");
        String stopScript = read(root, "scripts/stop-edge-kafka.sh");
        String runScript = read(root, "scripts/run-e2e-edge-kafka.sh");
        String workflow = read(root, ".github/workflows/scm-ci.yml");
        String rootWorkflow = read(root.getParent(), ".github/workflows/scm-platform-edge-kafka.yml");
        String kafkaOverlay = read(root, "docker-compose.kafka-overlay.yml");
        String compose = read(root, "docker-compose.yml");

        assertTrue(edgeKafkaCompose.contains("jwt,jwt-jwks,kafka,docker-kafka"));
        assertTrue(edgeKafkaCompose.contains("profiles: [\"legacy-gateway\"]"));
        assertUsesEdgeKafkaComposeChain(startScript);
        assertUsesEdgeKafkaComposeChain(stopScript);
        assertTrue(runScript.contains("-Pe2e-kafka"));
        assertTrue(runScript.contains("@E2E-K05"));
        assertTrue(runScript.contains("SCM_E2E_OMS_AUTH"));
        assertTrue(startScript.contains("SCM_COMPOSE_SKIP_BUILD"));
        assertTrue(startScript.contains("--no-build"));
        assertKafkaOverlayDoesNotUseEmptyProfiles(kafkaOverlay);
        assertKafkaHasInternalAndExternalListeners(compose);
        assertEdgeStackAcceptsHostIssuedKeycloakTokens(edgeCompose);

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

    private static void assertEdgeStackAcceptsHostIssuedKeycloakTokens(String edgeCompose) {
        assertTrue(edgeCompose.contains("SCM_JWT_ISSUER: http://localhost:8180/realms/scm"));
        assertTrue(edgeCompose.contains("SCM_JWT_JWKS_URI: http://keycloak:8080/realms/scm/protocol/openid-connect/certs"));
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
        assertTrue(rootWorkflow.contains("up -d mysql-erp mysql-oms mysql-wms mysql-tms redis"));
        assertTrue(rootWorkflow.contains("Start Kafka infrastructure"));
        assertTrue(rootWorkflow.contains("up -d kafka"));
        assertTrue(rootWorkflow.contains("Start Keycloak infrastructure"));
        assertTrue(rootWorkflow.contains("up -d keycloak"));
        assertTrue(rootWorkflow.contains("Start ERP and TMS services"));
        assertTrue(rootWorkflow.contains("up -d --no-build scm-erp scm-tms"));
        assertTrue(rootWorkflow.contains("Wait for ERP and TMS ports"));
        assertTrue(rootWorkflow.contains("Start OMS and WMS services"));
        assertTrue(rootWorkflow.contains("up -d --no-build scm-oms scm-wms"));
        assertTrue(rootWorkflow.contains("Wait for OMS and WMS ports"));
        assertTrue(rootWorkflow.contains("Start mock services"));
        assertTrue(rootWorkflow.contains("up -d --no-build scm-mock-pay scm-mock-carrier scm-mock-inventory"));
        assertTrue(rootWorkflow.contains("Start JWT gateway service"));
        assertTrue(rootWorkflow.contains("up -d --no-build scm-gateway-jwt"));
        assertTrue(rootWorkflow.contains("bash scripts/run-e2e-edge-kafka.sh"));
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
