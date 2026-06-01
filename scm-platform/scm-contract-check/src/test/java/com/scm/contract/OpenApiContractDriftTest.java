package com.scm.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对比 ai-dev OpenAPI 与各服务测试 JSON Schema 的 required 字段，防止契约漂移。
 */
class OpenApiContractDriftTest {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON = new ObjectMapper();

    @TestFactory
    Stream<DynamicTest> requiredFieldsMatchBindings() throws Exception {
        Path platformRoot = resolvePlatformRoot();
        Path aiDevContracts = resolveAiDevContracts(platformRoot);
        List<Binding> bindings = loadBindings();
        List<DynamicTest> tests = new ArrayList<>();
        for (Binding binding : bindings) {
            tests.add(DynamicTest.dynamicTest(binding.name, () -> {
                Set<String> openapiRequired = readOpenApiRequired(aiDevContracts, binding);
                Set<String> schemaRequired = readJsonSchemaRequired(platformRoot.resolve(binding.testSchema));
                assertEquals(openapiRequired, schemaRequired,
                        () -> "OpenAPI 与测试 Schema required 不一致: " + binding.name);
            }));
        }
        return tests.stream();
    }

    private static Path resolvePlatformRoot() {
        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (cwd.getFileName().toString().equals("scm-contract-check")) {
            return cwd.getParent();
        }
        return cwd;
    }

    private static Path resolveAiDevContracts(Path platformRoot) {
        Path fromProp = System.getProperty("scm.aiDevContracts") != null
                ? Paths.get(System.getProperty("scm.aiDevContracts"))
                : null;
        if (fromProp != null && Files.isDirectory(fromProp)) {
            return fromProp.toAbsolutePath().normalize();
        }
        Path sibling = platformRoot.getParent().resolve("业务方案").resolve("ai-dev").resolve("contracts");
        if (Files.isDirectory(sibling)) {
            return sibling;
        }
        Path embedded = platformRoot.resolve("scm-contract-check/src/test/resources/openapi");
        assertTrue(Files.isDirectory(embedded),
                "未找到 ai-dev contracts，请运行 scripts/sync-openapi-contracts.ps1");
        return embedded;
    }

    private static List<Binding> loadBindings() throws Exception {
        try (InputStream in = OpenApiContractDriftTest.class.getResourceAsStream("/contract-bindings.json")) {
            Binding[] arr = JSON.readValue(in, Binding[].class);
            return List.of(arr);
        }
    }

    private static Set<String> readOpenApiRequired(Path contractsDir, Binding binding) throws Exception {
        Path openapiFile = contractsDir.resolve(binding.openapi);
        assertTrue(Files.exists(openapiFile), "缺少 OpenAPI: " + openapiFile);
        JsonNode root = YAML.readTree(Files.readString(openapiFile));
        JsonNode requiredNode;
        if (binding.componentSchema != null && !binding.componentSchema.isBlank()) {
            requiredNode = root.path("components").path("schemas").path(binding.componentSchema).path("required");
        } else {
            JsonNode paths = root.get("paths");
            JsonNode pathItem = paths != null ? paths.get(binding.path) : null;
            JsonNode op = pathItem != null ? pathItem.get(binding.method) : null;
            JsonNode schema = op == null ? null : op.path("requestBody").path("content")
                    .path("application/json").path("schema");
            if (schema == null || schema.isMissingNode()) {
                requiredNode = null;
            } else if (schema.has("$ref")) {
                String ref = schema.get("$ref").asText();
                String name = ref.substring(ref.lastIndexOf('/') + 1);
                requiredNode = root.path("components").path("schemas").path(name).path("required");
            } else {
                requiredNode = schema.path("required");
            }
        }
        return toSortedSet(requiredNode);
    }

    private static Set<String> readJsonSchemaRequired(Path schemaPath) throws Exception {
        assertTrue(Files.exists(schemaPath), "缺少测试 Schema: " + schemaPath);
        JsonNode root = JSON.readTree(Files.readString(schemaPath));
        return toSortedSet(root.path("required"));
    }

    private static Set<String> toSortedSet(JsonNode requiredNode) {
        Set<String> set = new TreeSet<>();
        if (requiredNode != null && requiredNode.isArray()) {
            for (JsonNode n : requiredNode) {
                set.add(n.asText());
            }
        }
        return set;
    }

    private record Binding(
            String name,
            String openapi,
            String componentSchema,
            String path,
            String method,
            String testSchema
    ) {
        Binding {
            if (componentSchema == null) {
                componentSchema = "";
            }
            if (path == null) {
                path = "";
            }
            if (method == null) {
                method = "";
            }
        }
    }
}
