# 波次 W21 摘要

## 1. WMS 库存分配（替代 mock-inventory 可选）

- WMS 暴露 `/inventory/v1`（reserve / confirm / release / status），契约对齐 openapi-inventory
- OMS `scm.inventory.provider`：`local` | `mock` | `wms`
- 默认联调仍为 **mock**；生产 profile 默认 **wms**
- `start-all.ps1 -WmsInventory` 为 OMS 启用 `wms-inventory` profile

## 2. 网关 JWT / mTLS

- `deploy/gateway-jwt.sample.yaml` — OAuth2/JWT 与 scope
- `deploy/mtls-integration.md` — 东西向 mTLS 说明

## 3. 覆盖率

- Maven profile `-Pcoverage` 生成 JaCoCo 报告
- `scripts/check-coverage.ps1` — 行覆盖率阈值（默认 35%）
- `sonar-project.properties` — 可选 Sonar 扫描

## 验证

```powershell
mvn -q -pl scm-oms-service,scm-wms-service,scm-contract-check test
.\scripts\start-all.ps1 -WmsInventory
.\scripts\run-e2e-wms-inventory.ps1
mvn -Pcoverage verify
.\scripts\check-coverage.ps1
```
