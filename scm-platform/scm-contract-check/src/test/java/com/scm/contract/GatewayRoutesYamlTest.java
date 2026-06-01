package com.scm.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 校验 deploy 网关样例路由与四服务端口约定一致。 */
class GatewayRoutesYamlTest {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    @Test
    void gatewayRoutesCoverCoreServices() throws Exception {
        Path platformRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (platformRoot.getFileName().toString().equals("scm-contract-check")) {
            platformRoot = platformRoot.getParent();
        }
        Path yaml = platformRoot.resolve("deploy/gateway-routes.sample.yaml");
        assertTrue(Files.exists(yaml), "缺少 gateway-routes.sample.yaml");
        JsonNode root = YAML.readTree(Files.readString(yaml));
        Set<String> upstreams = new TreeSet<>();
        for (JsonNode route : root.path("routes")) {
            upstreams.add(route.path("upstream").asText());
        }
        assertTrue(upstreams.stream().anyMatch(u -> u.contains(":8081")), "应包含 OMS");
        assertTrue(upstreams.stream().anyMatch(u -> u.contains(":8082")), "应包含 WMS");
        assertTrue(upstreams.stream().anyMatch(u -> u.contains(":8083")), "应包含 TMS");
        assertTrue(upstreams.stream().anyMatch(u -> u.contains(":8084")), "应包含 ERP");
        assertFalse(root.path("headers").path("pass_through").isEmpty());
    }
}
