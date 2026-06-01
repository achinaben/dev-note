# JWT / OAuth2 与 OMS 资源 scope 联调（W22）

## 启动 Keycloak

```powershell
cd scm-platform
docker compose -f docker-compose.keycloak.yml up -d
# 管理台 http://localhost:8180  用户 admin / admin
```

或使用 `scripts/start-keycloak.ps1`。

## 获取 access_token（密码模式，仅开发）

```bash
curl -s -X POST "http://localhost:8180/realms/scm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=scm-gateway" \
  -d "username=e2e-user" \
  -d "password=e2e-pass" \
  -d "scope=openid oms.write"
```

响应中的 `access_token` 用于调用网关或 OMS。

## 经网关访问 OMS（样例）

`deploy/gateway-jwt.sample.yaml` 定义了 scope `oms.write` 与路由 `/api/v1/orders`。

Nginx / APISIX 需配置 JWT 校验插件，校验：

- `iss` = `https://auth.example.com/realms/scm`（本地改为 `http://localhost:8180/realms/scm`）
- `aud` 包含 `scm-api` 或 client
- `scope` / `realm_access.roles` 含 `oms.write`

开发期可直接带 Bearer 调 OMS（不经网关）验证业务：

```bash
curl -s -H "Authorization: Bearer <access_token>" \
  -H "Idempotency-Key: ct-jwt-1" \
  -H "Content-Type: application/json" \
  -d '{"client_token":"ct-jwt-1","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
       "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}' \
  http://localhost:8081/api/v1/orders
```

## RS256 验签（预发 / E2E-JWT02）

OMS 叠加 Spring profiles `jwt,jwt-jwks`：

- `verify-signature: true`
- `jwks-uri: http://localhost:8180/realms/scm/protocol/openid-connect/certs`

```powershell
.\scripts\start-keycloak.ps1
.\scripts\start-all.ps1 -OmsJwt -OmsJwtJwks
$env:SCM_JWT_STRICT = "1"
.\scripts\run-e2e-jwt-jwks.ps1
```

无 Keycloak 时 `e2e-jwt` 仍跑 E2E-JWT01（unsigned）；E2E-JWT02 在 `e2e-jwt-jwks` 中强制通过。

## Scope 对照（手册口径）

| Scope | 允许操作 |
|-------|----------|
| `oms.write` | 建单、支付回调、履约 |
| `oms.read` | 查单、诊断（ops 关闭时不可用） |
| `wms.write` | 出库创建、发运 |

## 与 mTLS

南北向 JWT 鉴权用户/应用；东西向服务见 `mtls-integration.md`。生产建议二者叠加。
