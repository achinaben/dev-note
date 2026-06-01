package com.scm.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 锁定 W37 OpenResty 直连 JWKS 验签配置，避免回退到 OMS auth_request。
 */
class OpenRestyJwksConfigTest {

    @Test
    void openRestyVerifiesJwtByDirectJwks() throws Exception {
        Path root = resolvePlatformRoot();
        String dockerfile = read(root, "deploy/openresty/Dockerfile");
        String lua = read(root, "deploy/openresty/jwt-auth.lua");
        String nginx = read(root, "deploy/openresty/nginx.conf");
        String nginxHost = read(root, "deploy/openresty/nginx.host.conf");
        String edgeCompose = read(root, "docker-compose.edge.yml");
        String gatewayCompose = read(root, "docker-compose.gateway-jwt.yml");
        String localCompose = read(root, "docker-compose.gateway-jwt-local.yml");

        assertTrue(dockerfile.contains("lua-resty-openidc"));
        assertTrue(lua.contains("require \"resty.openidc\""));
        assertTrue(lua.contains("bearer_jwt_verify"));
        assertTrue(lua.contains("SCM_JWT_JWKS_URI"));
        assertTrue(lua.contains("token_signing_alg_values_expected"));

        assertUsesJwksCache(nginx);
        assertUsesJwksCache(nginxHost);
        assertNoOmsJwtSubrequest(nginx);
        assertNoOmsJwtSubrequest(nginxHost);

        assertTrue(edgeCompose.contains("SCM_JWT_JWKS_URI"));
        assertTrue(gatewayCompose.contains("SCM_JWT_JWKS_URI"));
        assertTrue(localCompose.contains("SCM_JWT_JWKS_URI"));
    }

    private static void assertUsesJwksCache(String nginx) {
        assertTrue(nginx.contains("lua_shared_dict jwks"));
        assertTrue(nginx.contains("resolver 127.0.0.11"));
    }

    private static void assertNoOmsJwtSubrequest(String nginx) {
        assertFalse(nginx.contains("auth_request /_scm_jwt_rs256"));
        assertFalse(nginx.contains("/internal/v1/jwt/check"));
    }

    private static String read(Path root, String relativePath) throws Exception {
        return Files.readString(root.resolve(relativePath));
    }

    private static Path resolvePlatformRoot() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (cwd.getFileName().toString().equals("scm-contract-check")) {
            return cwd.getParent();
        }
        return cwd;
    }
}
