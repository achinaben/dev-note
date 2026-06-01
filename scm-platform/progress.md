# scm-platform 自动化进度（Automation / Agent 续跑用）

> 每次 Automation 或手动 Agent 跑完一轮后更新本文件。**不要删历史**，在底部追加。

## 当前状态

| 项 | 值 |
|----|-----|
| 目标波次 | W37 |

| 上次更新 | 2026-06-01 19:04 UTC |
| 上次 mvn test | `mvn test` 失败于 TMS WireMock/Jetty；`mvn -pl scm-contract-check -am test` 通过 |
| 阻塞项 | 全量测试在 TMS WireMock/Jetty 依赖冲突处失败；本轮 OpenResty 契约测试不受影响 |

| 触发频率 | 每分钟 `* * * * *`（见 提示词/提示词.md） |

## W37 清单

- [x] OpenResty 内嵌 lua-resty-openidc 直连 JWKS
- [ ] 全栈 E2E-06（售后拦截）
- [x] Kafka profile 纳入 compose（docker-compose.kafka-overlay.yml + application-docker-kafka）
- [ ] E2E-K05 在 compose 栈上 CI 跑通

## W36 清单

- [x] OpenResty RS256（auth_request + OMS jwt-jwks）+ lua scope/realm roles
- [x] docker-compose.full mock 三侧车 + edge（Keycloak+网关）
- [x] CI docker-stack-smoke / e2e-stack-smoke
- [x] GatewayRoutesOpenApiTest + 覆盖率 50%

## 运行日志

### 2026-06-01 Agent W36

- OpenResty：`jwt-auth.lua` require_bearer；`nginx.conf` auth_request RS256
- compose：`docker-compose.edge.yml`；full 增加 mock 8085-8087
- 脚本：`start-edge-full`、`run-e2e-stack-smoke`
- 契约：`GatewayRoutesOpenApiTest`；E2E-GW04 与 GW03 去重
- CI：coverage 50%；e2e-stack-smoke job
- 下一动作：W37 全栈 E2E-06 或 lua-resty-openidc

### 2026-06-01 用户反馈：Automation 未自动继续

- 原因：① Cursor 未启用 Automation；② 旧指令在 W36 完成后一律跳过；③ 阻塞项写法导致每轮提前退出
- 已修：instructions/prefill 改 W37 + */5 cron；提示词.md 说明启用步骤
- 下一动作：W37 E2E-06 或 edge+kafka 一键脚本

### 2026-06-01 Agent（Kafka overlay）

- 新增 docker-compose.kafka-overlay.yml、application-docker-kafka（三服务）
- mvn test：未全量跑（本机 JDK25 WireMock）


### 2026-06-01 Agent（OpenResty direct JWKS）

- 完成：OpenResty 内嵌 lua-resty-openidc，使用 `SCM_JWT_JWKS_URI` 直连 JWKS 验 RS256；保留 iss、exp、scope 与 Keycloak realm roles 校验。
- 网关：移除 OMS JWT 子请求校验，补充 JWKS 缓存、Docker DNS resolver、CA 证书配置；compose 为 edge、本地网关补齐 JWKS 地址。
- 契约：新增 OpenResty JWKS 配置测试，防止回退到 OMS auth_request。
- 测试：`mvn -pl scm-contract-check -am test` 通过；`mvn test` 在 TMS WireMock/Jetty 依赖冲突处失败。
- 下一动作：全栈 E2E-06（售后拦截）。
