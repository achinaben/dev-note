package com.scm.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 网关样例路由 path 应在同步的 OpenAPI 中出现（防漂移）。
 */
class GatewayRoutesOpenApiTest {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    @Test
    void gatewaySamplePathsExistInOpenApi() throws Exception {
        Path root = resolvePlatformRoot();
        Path routesFile = root.resolve("deploy/gateway-routes.sample.yaml");
        Path openapiDir = root.resolve("scm-contract-check/src/test/resources/openapi");
        assertTrue(Files.isRegularFile(routesFile));
        assertTrue(Files.isDirectory(openapiDir));

        JsonNode routes = YAML.readTree(Files.readString(routesFile));
        List<String> routePaths = new ArrayList<>();
        for (JsonNode r : routes.get("routes")) {
            routePaths.add(r.get("path").asText());
        }

        StringBuilder allOpenApi = new StringBuilder();
        try (Stream<Path> files = Files.list(openapiDir)) {
            files.filter(p -> p.toString().endsWith(".yaml")).forEach(p -> {
                try {
                    allOpenApi.append(Files.readString(p));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        String corpus = allOpenApi.toString();
        for (String routePath : routePaths) {
            String prefix = routePath.replace("/**", "").replace("**", "");
            String alt0 = prefix.startsWith("/api/v1") ? prefix.substring("/api/v1".length()) : prefix;
            final String alt = alt0.isEmpty() ? prefix : alt0;
            assertTrue(corpus.contains(prefix) || corpus.contains(alt),
                    () -> "OpenAPI 中未找到网关路由前缀: " + prefix + " / " + alt + " (来自 " + routePath + ")");
        }
    }

    private static Path resolvePlatformRoot() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (cwd.getFileName().toString().equals("scm-contract-check")) {
            return cwd.getParent();
        }
        return cwd;
    }
}
