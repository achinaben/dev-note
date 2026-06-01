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
        assertUsesEdgeKafkaComposeChain(startScript);
        assertUsesEdgeKafkaComposeChain(stopScript);
        assertTrue(runScript.contains("-Pe2e-kafka"));
        assertTrue(runScript.contains("@E2E-K05"));
        assertTrue(runScript.contains("SCM_E2E_OMS_AUTH"));
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
        assertTrue(compose.contains("9092:29092"));
        assertTrue(compose.contains("PLAINTEXT://kafka:9092"));
        assertTrue(compose.contains("EXTERNAL://localhost:9092"));
        assertTrue(compose.contains("KAFKA_CFG_INTER_BROKER_LISTENER_NAME: PLAINTEXT"));
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
        assertTrue(rootWorkflow.contains("bash scripts/start-edge-kafka.sh"));
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
