
# OpenResty 网关 — JWT RS256/JWKS 校验 + X-Api-Key 南北向鉴权
FROM openresty/openresty:1.25.3.2-alpine
RUN apk add --no-cache ca-certificates curl perl \
    && opm get zmartzone/lua-resty-openidc

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY jwt-auth.lua /etc/nginx/lua/jwt-auth.lua
ENV SCM_JWT_ISSUER=http://keycloak:8080/realms/scm
ENV SCM_JWT_JWKS_URI=http://keycloak:8080/realms/scm/protocol/openid-connect/certs

# RS256：OpenResty 通过 lua-resty-openidc 直连 JWKS 验签。

EXPOSE 8080
