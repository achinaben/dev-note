# AGENTS.md — scm-platform

## 当前波次

**W36 已完成**：OpenResty RS256（auth_request→OMS）、全栈 mock、边缘 compose、网关路由契约测试、覆盖率 50%、CI e2e-stack-smoke。

**W37 进行中**：OpenResty 已内嵌 lua-resty-openidc 直连 JWKS；全栈 E2E-06 已跑通；edge + kafka 一键脚本与 CI job 已补，下一项观察并修复 E2E-K05 compose CI。

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


- `deploy/openresty/jwt-auth.lua` — lua-resty-openidc 直连 JWKS 验 RS256，并校验 exp / iss / scope（含 Keycloak realm roles）
- OpenResty 环境变量：`SCM_JWT_ISSUER`、`SCM_JWT_JWKS_URI`

- 建单路由 **必须** 带 Bearer（`require_bearer`）
- E2E-GW03：错误 scope → 403；E2E-GW04：无 Bearer → 401

## Docker 全栈

| 组合 | 内容 |
|------|------|
| `docker-compose.full.yml` | MySQL + 四服务 + mock-pay/carrier/inventory |

=======
| `docker-compose.edge.yml` | + Keycloak + OpenResty :8089 直连 JWKS |


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
4. E2E-K05 compose CI 运行结果观察与修复（下一轮）

## Cursor Cloud specific instructions

- **JDK**：必须使用 **17**（与 GitHub Actions 一致）。云 VM 默认可能是 21；在 shell 中设置 `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64` 并把 `$JAVA_HOME/bin` 置于 `PATH` 最前。JDK 22+ 上 TMS 的 WireMock Jetty12 测试会因 ICCE 失败或被 `@EnabledIf` 跳过。
- **构建/单测**：在 `scm-platform` 目录执行 `mvn test`（全模块）。契约检查：`bash scripts/sync-openapi-contracts.sh && mvn -pl scm-contract-check test`。
- **起服务（内存模式，无 Docker）**：`cd scm-platform/scripts && bash start-all.sh`（约 1–3 分钟，会先 `mvn install -DskipTests`）。就绪标志为 **8081–8087** 均可 `nc`/`/dev/tcp` 连通。日志在 `scripts/logs/*.log`。停止：`bash scripts/stop-all.sh`。
- **Hello world**：OMS `:8081` 上 `POST /api/v1/orders`（带 `Idempotency-Key`）→ `POST /api/v1/payments/notify/wechat` → `GET /api/v1/orders/{orderNo}` 应看到 `PAID`。
- **E2E**：服务就绪后 `mvn -pl scm-integration-tests -Pe2e test`，或一键 `bash scripts/ci-e2e.sh`（含单测+起服+E2E+停服）。
- **Docker 可选**：本仓库 Kafka/MySQL/全栈 compose 需宿主机 Docker；云 VM 若未装 Docker，仅用 `start-all.sh` 即可验证核心 B2C API。
- **TMS 测试依赖**：`scm-tms-service` 仅应声明 `wiremock-jetty12`（勿与 `wiremock` 主构件同时引入，否则 Jetty 11/12 混用导致 WireMock 启动 ICCE）。


