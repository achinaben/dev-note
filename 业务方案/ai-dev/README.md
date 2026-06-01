# ai-dev 契约与测试资产

供 **供应链四系统 AI 自动开发与测试手册** 使用。

## 目录

| 路径 | 用途 |
|------|------|
| `fixtures.yaml` | 全链路固定 ID，禁止测试随意改名 |
| `e2e.feature` | Cucumber 10 条 Scenario，W6 退出标准 |
| `e2e-kafka.feature` | Kafka 子集 E2E-K01~K11（无 ops /ship） |
| `e2e-jdbc.feature` | jdbc 子集 E2E-J01~J08 |
| `docker-compose.yml` | 本地 MySQL×4 + Redis + Kafka |
| `seed/W1_master_data.sql` | W1 主数据种子 |
| `contracts/openapi-*.yaml` | 契约测试来源（RestAssured + OpenAPI） |
| `contracts/event-envelope.schema.json` | 事件信封校验 |
| `contracts/events/*.schema.json` | 各事件 data 校验 |
| `UNIT_TEST_CATALOG.md` | 单测/集成测类名清单（AI 逐项勾选） |
| `cucumber-step-definitions.md` | E2E Step 实现模板 |

## AI 第一步

1. **可运行代码仓**已生成在笔记同级目录 **`scm-platform/`**（与本文档配套）。
2. 执行手册 **W0**：`cd scm-platform && docker compose up -d`（需 JDK **17**）。
3. `mvn -q test` 全绿后，按 `AGENTS.md` 推进；E2E 用 `scm-platform/scripts` 一键脚本。
4. 契约 YAML 以本目录为准；实现时保持字段一致。
5. **CI**：`scm-ci.yml`（单测 + E2E + docker-it）。
6. **Kafka E2E**：`.\start-all.ps1 -Kafka` 后 `mvn -pl scm-integration-tests -Pe2e-kafka test`。
7. **远程库存（W20）**：`start-all` 启动 8087；E2E-01 校验 mock-inventory CONFIRMED。
8. **WMS 库存（W21）**：`.\start-all.ps1 -WmsInventory` + `run-e2e-wms-inventory.ps1`（E2E-12）；生产默认 `provider=wms`。
9. **JWT（W23/W24）**：开发仅 claims；生产 `verify-signature=true` + JWKS。
10. **严格出库（W24）**：`.\start-all.ps1 -WmsStrict` + `run-e2e-strict-outbound.ps1`（E2E-13）。

## 端口约定

| 服务 | 端口 |
|------|------|
| OMS | 8081 |
| WMS | 8082 |
| TMS | 8083 |
| ERP | 8084 |
| mock-pay | 8085 |
| mock-carrier | 8086 |
| mock-inventory | 8087 |
