package com.scm.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 防止 OpenResty JWT 网关从直连 JWKS 回退到 OMS auth_request。 */
class OpenRestyJwtGatewayTest {

    @Test
    void openRestyVerifiesJwtWithOpenidcAndJwks() throws Exception {
        Path root = resolvePlatformRoot();
        String dockerfile = Files.readString(root.resolve("deploy/openresty/Dockerfile"));
        String jwtAuth = Files.readString(root.resolve("deploy/openresty/jwt-auth.lua"));
        String nginx = Files.readString(root.resolve("deploy/openresty/nginx.conf"));
        String nginxHost = Files.readString(root.resolve("deploy/openresty/nginx.host.conf"));
        String edgeCompose = Files.readString(root.resolve("docker-compose.edge.yml"));
        String localCompose = Files.readString(root.resolve("docker-compose.gateway-jwt-local.yml"));

        assertTrue(dockerfile.contains("luarocks install lua-resty-openidc"));
        assertTrue(jwtAuth.contains("require \"resty.openidc\""));
        assertTrue(jwtAuth.contains("bearer_jwt_verify"));
        assertTrue(jwtAuth.contains("SCM_JWT_JWKS_URI"));

        assertOpenRestyConfigUsesDirectJwks(nginx);
        assertOpenRestyConfigUsesDirectJwks(nginxHost);

        assertTrue(edgeCompose.contains("SCM_JWT_JWKS_URI: http://keycloak:8080/realms/scm/protocol/openid-connect/certs"));
        assertTrue(localCompose.contains("SCM_JWT_JWKS_URI: http://host.docker.internal:8180/realms/scm/protocol/openid-connect/certs"));
    }

    private static void assertOpenRestyConfigUsesDirectJwks(String config) {
        assertTrue(config.contains("lua_shared_dict jwks"));
        assertTrue(config.contains("lua_shared_dict jwt_verification"));
        assertFalse(config.contains("auth_request /_scm_jwt_rs256"));
        assertFalse(config.contains("/internal/v1/jwt/check"));
    }

    private static Path resolvePlatformRoot() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (cwd.getFileName().toString().equals("scm-contract-check")) {
            return cwd.getParent();
        }
        return cwd;
    }
}
