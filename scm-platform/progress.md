# scm-platform 自动化进度（Automation / Agent 续跑用）

> 每次 Automation 或手动 Agent 跑完一轮后更新本文件。**不要删历史**，在底部追加。

## 当前状态

| 项 | 值 |
|----|-----|
| 目标波次 | W37 |
| 上次更新 | 2026-06-01 |
| 上次 mvn test | scm-contract-check 局部通过（11 tests） |
| 阻塞项 | 无；当前云端无 docker 命令，仅影响本机镜像构建验证 |
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

### 2026-06-01 Agent W37（OpenResty direct JWKS）

- 完成：OpenResty 安装 lua-resty-openidc，网关通过 JWKS 直校 RS256 Bearer，不再转发 OMS auth_request。
- 配置：边缘栈与本地网关传入 JWKS 地址；保留 scope 与 realm roles 校验。
- 测试：`mvn -pl scm-contract-check test` 通过，11 tests。
- 说明：云端无 docker 命令，OpenResty 镜像构建未在本机验证；下次继续 W37 全栈 E2E-06（售后拦截）。
