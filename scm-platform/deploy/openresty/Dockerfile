# OpenResty 网关 — JWT 声明校验（exp/scope）+ X-Api-Key 南北向鉴权
FROM openresty/openresty:1.25.3.2-alpine
RUN apk add --no-cache curl
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY jwt-auth.lua /etc/nginx/lua/jwt-auth.lua
ENV SCM_JWT_ISSUER=http://keycloak:8080/realms/scm
# RS256：auth_request 转发至 OMS /internal/v1/jwt/check（需 OMS profiles jwt,jwt-jwks）
EXPOSE 8080
