# 波次 W22 摘要

## 1. WMS FEFO 与 jdbc 持久化

- `FefoAllocator`：按效期升序分配
- `MemoryStockRepository` / `JdbcStockRepository` + 预占明细表（Flyway V2）
- `AllocationServiceFefoTest`、`AllocationConcurrentTest`、`WmsInventoryJdbcIT`

## 2. JWT 联调

- `docker-compose.keycloak.yml` + `deploy/keycloak/scm-realm.json`
- `deploy/jwt-oms-integration.md` + `scripts/start-keycloak.ps1`

## 3. CI

- `coverage` job：`mvn -Pcoverage verify` + JaCoCo 产物上传
- `sonar` job：配置 `SONAR_TOKEN` 后可选扫描

## 验证

```powershell
mvn -q -pl scm-wms-service test "-Dtest=AllocationServiceFefoTest,AllocationConcurrentTest,InventoryAllocationContractTest"
mvn -q -pl scm-wms-service -Pdocker-it test "-Dtest=WmsInventoryJdbcIT"
.\scripts\start-keycloak.ps1
```
