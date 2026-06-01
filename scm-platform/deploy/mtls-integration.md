# 系统集成 mTLS 说明（样例）

## 适用场景

- ERP ← WMS 发运过账、TMS ← 承运商回调、OMS ← 支付/库存 等**服务间**调用。
- 与 `gateway-jwt.sample.yaml`（南北向用户/应用）互补：mTLS 解决东西向身份。

## 证书分工

| 角色 | 证书 | 校验 |
|------|------|------|
| 客户端 | `client.crt` + `client.key` | 服务端信任 CA |
| 服务端 | `server.crt` + `server.key` | 客户端信任同一 CA |

本地可用 `openssl` 自签 CA（仅开发）：

```bash
openssl req -x509 -newkey rsa:2048 -keyout ca.key -out ca.crt -days 365 -nodes -subj "/CN=scm-dev-ca"
openssl req -newkey rsa:2048 -keyout server.key -out server.csr -nodes -subj "/CN=scm-erp"
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt -days 365
```

## Nginx 服务端样例片段

```nginx
ssl_client_certificate /etc/nginx/ca.crt;
ssl_verify_client optional;
ssl_protocols TLSv1.2 TLSv1.3;

location /api/v1/integration/ {
    if ($ssl_client_verify != SUCCESS) { return 403; }
    proxy_pass http://127.0.0.1:8084;
}
```

## Spring Boot 服务端（ERP 集成端口）

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:server.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    trust-store: classpath:truststore.p12
    client-auth: need
```

## 与现有头的关系

- **mTLS**：证明“是哪台机器/服务”。
- **X-Integration-Key / X-Ops-Token**：应用层二次校验，prod 已启用 integration key。
- **X-Org-Id**：租户，与传输层证书无关。

## 联调建议

1. 默认 `start-all` 仍用 HTTP，降低本地门槛。
2. 预发再开 mTLS；契约测试与 E2E 继续走 HTTP + API Key。
3. 证书轮换：双证并行 7 天，网关与服务同时信任新旧 CA。
