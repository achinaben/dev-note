# scm-platform

供应链 **ERP / OMS / WMS / TMS** 可运行工程，与笔记「业务方案」中的详细设计、**ai-dev 契约**、**AI 自动开发与测试手册** 对齐。

## 环境要求

- **JDK 17+**（必须；JDK 8 无法编译）
- Maven 3.8+
- Docker（W5+ 集成测，可选）

## 模块（W19）

- **scm-mock-inventory** `:8087`；OMS `scm.inventory.remote-enabled` + **InventoryRemoteWireMockTest**
- **scripts**：`stress-b2c.ps1`、`export-openapi.ps1`；**deploy/gateway-routes.sample.yaml**
- **X-Org-Id** 透传 + `OrgIdPassthroughTest` / `OrgIdPostingTest`

## 模块（W18）

- **OMS** 发运自动 TMS 时按运费 **recommended** 选承运商（默认 YTO）
- **E2E-11 / K11 / J08**：mock-carrier `trigger/track` → OMS **DELIVERED**
- **prod**：ops 关闭；可选 `X-Ops-Token` / `X-Integration-Key`

## 模块（W17）

- **scm-mock-carrier** `:8086` — `/carrier/v1/quote`、`/trigger/track`
- **TMS** 运费 `recommended` 择优、`POST /shipment/{no}/intercept`
- **E2E-K06~K10**、**E2E-J03~J07**（对齐全量 E2E-07~10 + 退款）

## 模块（W16）

- **TMS** `/freight/estimate`、`/integration/carrier/{code}/callback`；**WireMock** 承运商/OMS 转发测
- **OMS** `TmsTrackContractTest`；**WMS** `GET /outbound/by-package/{packageNo}`
- **E2E-K05** Kafka 双包裹部分发运；**CI** 独立 `e2e-kafka` / `e2e-jdbc` job

## 模块（W15）

- **OMS** 发运后 `scm.fulfillment.auto-tms-on-ship` 自动调 TMS `/shipment/create`
- **E2E-K04**、**e2e-jdbc.feature**（J01/J02）；`start-all.ps1 -Jdbc`、`run-e2e-jdbc.ps1`
- **scm-contract-check**：OpenAPI `required` 与测试 JSON Schema 漂移检测（CI 已接入）
- **CreateShipmentContractTest**（TMS）

## 模块（W14）

- **OMS** `WmsOutboundShippedHandler`：Kafka 发运后订单 SHIPPED（E2E-K03）
- **mock-pay** 接入 E2E-01；`start-all` 默认起 :8085
- **CreateOutboundContractTest**、**WmsShipmentPostContractTest**

## 模块（W13）

- **CreateOrderContractTest**：JSON Schema 校验建单请求/响应
- **OutboxPollerIT**：MySQL Outbox + Kafka relay（`-Pdocker-it`）
- **e2e-kafka.feature**：E2E-K01/K02，WMS 发运替代 ops `/ship`
- **CI**：新增 `docker-it` job

## 模块（W12）

- **CI**：`.github/workflows/scm-ci.yml`（`mvn test` + E2E job）
- **scripts**：`start-all.sh` / `ci-e2e.sh` / `stop-all.sh`；Windows `start-all.ps1 -Kafka`
- **单测**：`CreditCheckContractTest`、`TmsTrackRankTest`；`UNIT_TEST_CATALOG` 已同步勾选

## 模块（W11）

- **e2e.feature**：E2E-01~10 步骤与 ai-dev 契约一致
- **scripts/**：`start-all.ps1`、`run-e2e.ps1`、`stop-all.ps1`

## 模块（W10）

- **TMS**：`ShipmentStore` + Flyway `tms_shipment`，profile `jdbc` 端口 3310
- **Testcontainers**：`*JdbcIT.java`，`mvn -Pdocker-it test`（需 Docker）
- **E2E-02**：`GET /api/v1/ops/orders/trade-count` 断言仅 1 条订单

## 模块（W9）

- WMS **OutboundStore**（memory / jdbc），`GET /wms/v1/ops/outbound/count` 支撑 E2E-07
- **`scm.ops.enabled`**：生产 profile `prod` 默认关闭联调接口

## 模块（W8）

- **Flyway + MySQL**：各服务 `application-jdbc.yml`，启动加 `-Dspring-boot.run.profiles=jdbc`
- **部分发货**：双包裹 `PARTIAL_SHIPPED` → `SHIPPED`
- **退货退款**：售后 + ERP 退款凭证

## 模块（W7）

- **scm-common-spring**：事件总线（`memory` 默认 / `kafka` profile）
- OMS：Outbox 定时投递 `ORDER_PAID`
- WMS：消费 `ORDER_PAID` 自动建出库单；发运可发 `WMS_OUTBOUND_SHIPPED`
- ERP：Kafka 消费发运事件过账（profile `kafka` 时 WMS 仅走事件）

## 快速开始

```bash
cd scm-platform
mvn -q test
```

Kafka 联调（需 docker kafka:9092）：

```bash
mvn -pl scm-oms-service,scm-wms-service,scm-erp-service spring-boot:run -Dspring-boot.run.profiles=kafka
```

**E2E（10 个场景，对齐 ai-dev 契约）**

```powershell
# 一键启动（Windows）
cd scm-platform\scripts
.\start-all.ps1          # 或 .\start-all.ps1 -Kafka
.\run-e2e.ps1
.\stop-all.ps1
```

```bash
# Linux / CI
bash scripts/ci-e2e.sh
```

Kafka 子集 E2E（需四服务 kafka profile）：

```powershell
.\start-all.ps1 -Kafka
.\run-e2e-kafka.ps1
```

或手动四终端分别 `mvn -pl <module> spring-boot:run`，再执行：

```bash
mvn -pl scm-integration-tests -Pe2e test
```

启动单服务（示例 OMS）：

```bash
mvn -pl scm-oms-service spring-boot:run
```

建单示例：

```bash
curl -X POST http://localhost:8081/api/v1/orders ^
  -H "Idempotency-Key: ct-fix-001" ^
  -H "Content-Type: application/json" ^
  -d "{\"client_token\":\"ct-fix-001\",\"buyer_id\":\"U10001\",\"channel\":\"APP\",\"address_id\":\"ADDR100\",\"lines\":[{\"sku_id\":\"SKU001\",\"qty\":\"2\",\"warehouse_id\":\"WH-SH-01\"}]}"
```

## 模块与端口

| 模块 | 端口 | 波次 |
|------|------|------|
| scm-oms-service | 8081 | W2 |
| scm-wms-service | 8082 | W3 |
| scm-tms-service | 8083 | W4 |
| scm-erp-service | 8084 | W1 |
| scm-mock-pay | 8085 | W2 |

## 当前实现进度

- [x] W0 scm-common（事件信封、Schema 校验、幂等存储）
- [x] W1 ERP 物料查询（骨架）
- [x] W2 OMS 建单/支付回调/状态机/幂等测
- [x] W3 WMS 出库创建幂等
- [x] W4 TMS 运单创建幂等
- [x] W5 ERP 出库过账 + 幂等 + WMS 发运调 ERP
- [x] W6 OMS 支付→履约→WMS→ERP→TMS；Cucumber + `B2CLocalFlowIT`（profile `e2e`）

## 契约来源

设计期契约在 `../业务方案/ai-dev/contracts/`；测试资源已部分同步到 `scm-common/src/test/resources`。

## AI 开发

阅读 `AGENTS.md` 与 `../业务方案/供应链四系统-AI自动开发与测试手册.md`，按 W0→W6 推进。
