# scm-platform 自动化进度（Automation / Agent 续跑用）

> 每次 Automation 或手动 Agent 跑完一轮后更新本文件。**不要删历史**，在底部追加。

## 当前状态

| 项 | 值 |
|----|-----|
| 目标波次 | W37 |

| 上次更新 | 2026-06-01 20:51 UTC |
| 上次 mvn test | `mvn test` 通过 |
| 阻塞项 | 无 |

| 触发频率 | 每分钟 `* * * * *`（见 提示词/提示词.md） |

## W37 清单

- [x] OpenResty 内嵌 lua-resty-openidc 直连 JWKS
- [x] 全栈 E2E-06（售后拦截）
- [x] Kafka profile 纳入 compose（docker-compose.kafka-overlay.yml + application-docker-kafka）
- [x] edge + Kafka 一键脚本与 CI job
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

### 2026-06-01 Cloud Automation Run（失败）

- 触发成功，但云端 /agent 工作区空，未检出 dev-note，找不到 scm-platform
- 阻塞：Automation 需绑定 repo achinaben/dev-note + 指令先 cd scm-platform
- 已更新 scm-wave-minute.instructions.txt 与 prefill gitConfig

### 2026-06-01 Cloud Automation Run（W37 E2E-06）

- 已在仓库根同步 `cursor/scm-wave`，远程已是最新；本地阻塞拉取的是上一轮生成的 target 产物，已还原后继续。
- 完成：全栈 E2E-06 售后拦截验收通过；OMS 调 TMS 重复建运单时将 `TMS_10001` 幂等重放视为可继续，送达流程不再 500。
- 修正：OpenResty JWT 契约测试同时接受 luarocks 与 OpenResty opm 安装 lua-resty-openidc，匹配当前 Dockerfile 实现。
- 测试：`mvn -pl scm-oms-service -Dtest=TmsFulfillmentClientTest test`、`mvn -pl scm-integration-tests -Pe2e test -Dcucumber.filter.tags='@E2E-06'`、`mvn -pl scm-contract-check test`、`mvn test` 均通过。
- 下一动作：edge + kafka 一键脚本与 CI job，继续推进 E2E-K05 在 compose 栈上跑通。

### 2026-06-01 Cloud Automation Run（edge + Kafka CI）

- 已在仓库根同步 `cursor/scm-wave`，远程已是最新；本地阻塞拉取的 target 产物已还原后继续。
- 完成：新增 edge + Kafka 组合 overlay，保留 OMS 的 JWT/JWKS 与 Kafka profile；新增 Linux/PowerShell 一键启动、停止与 E2E-K05 运行脚本。
- CI：新增 `e2e-edge-kafka-stack` job，启动 edge + Kafka compose 栈后只跑 E2E-K05；新增契约测试固定脚本、compose 链与 workflow 入口。
- 测试：`mvn -pl scm-contract-check test` 通过；`mvn test` 通过。当前云 VM 无 Docker CLI，compose 实跑需由 GitHub CI job 验证。
- 下一动作：观察并修复 `e2e-edge-kafka-stack` 的 compose 栈 E2E-K05 CI 运行结果。

### 2026-06-01 Cloud Automation Run（E2E-K05 compose CI 修复）

- 已在仓库根同步 `cursor/scm-wave`，远程已是最新；本轮启动时 target 生成物导致 pull rebase 被拒，已确认无业务源码脏文件并还原生成物后继续。
- 修复：Kafka compose 改为双监听，容器内服务通过 `kafka:9092` 获取 broker 元数据，宿主机与 CI 仍通过 `localhost:9092` 访问，避免 OMS/WMS/ERP 在 Docker 内收到 `localhost` 导致 Kafka 事件链路断开。
- 契约：Edge Kafka 栈配置测试补充 Kafka 内外监听断言，防止回退为单一 localhost advertised listener。
- 测试：`mvn -pl scm-contract-check test` 通过；`mvn test` 通过。当前云 VM 无 Docker CLI，无法本地起 compose 栈。
- 下一动作：观察 `e2e-edge-kafka-stack` 推送后的 CI 结果；若仍失败，按 CI 日志继续修复。
