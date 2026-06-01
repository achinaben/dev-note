# 波次 W15–W20 收尾摘要

## W15

- OMS 发运后自动 TMS；jdbc E2E（J01/J02）
- `scm-contract-check` OpenAPI required 漂移检测

## W16

- TMS 运费/承运商回调 + WireMock
- E2E-K05 Kafka 部分发运；CI `e2e-kafka` / `e2e-jdbc`

## W17

- `scm-mock-carrier` :8086；运费 recommended 择优；运单 intercept
- E2E-K06~K10 / J03~J07

## W18

- 发运按 recommended 承运商；mock-carrier 轨迹签收
- prod 关闭 ops + `X-Ops-Token` / `X-Integration-Key`

## W19

- `scm-mock-inventory` :8087；OMS 远程库存（联调默认开启）
- `OrgIdContext` + `X-Org-Id`；压测/OpenAPI 导出/网关路由样例

## W20

- OMS `scm.inventory.remote-enabled` 默认 **true**（单测 profile 仍为 false）
- E2E「远程库存已 Confirm」+ 健康检查 8085~8087
- `deploy/gateway-auth.sample.yaml`、`nginx-gateway.sample.conf`
- `UNIT_TEST_CATALOG` 与 `scripts/coverage-report.ps1` 收尾

## 验证命令

```powershell
cd scm-platform\scripts
.\start-all.ps1
.\run-e2e.ps1
mvn -q test
.\coverage-report.ps1
```
