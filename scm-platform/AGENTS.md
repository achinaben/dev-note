# AGENTS.md — scm-platform

## 当前波次

**W36 已完成**：OpenResty RS256（auth_request→OMS）、全栈 mock、边缘 compose、网关路由契约测试、覆盖率 50%、CI e2e-stack-smoke。

## 一键本地联调（Windows）

```powershell
cd scm-platform\scripts
.\start-all.ps1 -OmsJwt
.\run-e2e-jwt.ps1
.\start-docker-stack.ps1      # MySQL + 四服务 + mock（8081-8087）
.\start-edge-full.ps1         # 上者 + Keycloak + JWT 网关 :8089
.\start-gateway-jwt.ps1       # 仅 OpenResty :8089（宿主机四服务）
$env:SCM_GATEWAY_JWT_URL='http://localhost:8089'
.\run-e2e-gateway-jwt.ps1     # E2E-GW01~04
.\run-e2e-stack-smoke.ps1     # Docker 全栈 @smoke E2E
```

## 命令

```bash
docker compose -f docker-compose.yml -f docker-compose.full.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.full.yml -f docker-compose.edge.yml up -d --build
COVERAGE_MIN=0.50 ./scripts/check-coverage.sh
bash scripts/sync-openapi-contracts.sh && mvn -pl scm-contract-check test
```

## 网关 JWT（OpenResty）

- `deploy/openresty/jwt-auth.lua` — exp / iss / scope（含 Keycloak realm roles）
- `auth_request` → OMS `GET /internal/v1/jwt/check`（**RS256**，需 OMS `jwt,jwt-jwks`）
- 建单路由 **必须** 带 Bearer（`require_bearer`）
- E2E-GW03：错误 scope → 403；E2E-GW04：无 Bearer → 401

## Docker 全栈

| 组合 | 内容 |
|------|------|
| `docker-compose.full.yml` | MySQL + 四服务 + mock-pay/carrier/inventory |
| `docker-compose.edge.yml` | + Keycloak + OpenResty :8089 + OMS jwt-jwks |

CI：`docker-stack-smoke`、`e2e-stack-smoke`（`@smoke and @e2e`）。

## 契约与网关路由

- `scripts/sync-openapi-contracts.sh` 同步 ai-dev OpenAPI
- `GatewayRoutesOpenApiTest` 校验 `gateway-routes.sample.yaml` 与 OpenAPI 前缀一致

## 质量门禁

JaCoCo **50%**（见 docs/quality-gate.md）。

## Kafka 全栈（W37）

```bash
docker compose -f docker-compose.yml -f docker-compose.full.yml -f docker-compose.kafka-overlay.yml up -d
mvn -pl scm-integration-tests -Pe2e-kafka test
```

## 下一步（W37）

1. OpenResty 内嵌 lua-resty-openidc 直连 JWKS
2. 全栈 E2E-06（售后拦截）
3. edge + kafka 一键脚本与 CI job
