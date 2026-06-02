# scm-platform 自动化进度（Automation / Agent 续跑用）

> 每次 Automation 或手动 Agent 跑完一轮后更新本文件。**不要删历史**，在底部追加。

## 当前状态

| 项 | 值 |
|----|-----|
| 目标波次 | W37 |

| 上次更新 | 2026-06-02 06:16 UTC |
| 上次 mvn test | `mvn test` 通过 |
| 阻塞项 | 无；edge + Kafka E2E-K05 compose CI 已通过 |

| 触发频率 | 每分钟 `* * * * *`（见 提示词/提示词.md） |

## W37 清单

- [x] OpenResty 内嵌 lua-resty-openidc 直连 JWKS
- [x] 全栈 E2E-06（售后拦截）
- [x] Kafka profile 纳入 compose（docker-compose.kafka-overlay.yml + application-docker-kafka）
- [x] edge + Kafka 一键脚本与 CI job
- [x] E2E-K05 在 compose 栈上 CI 跑通

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

### 2026-06-01 Cloud Automation Run（E2E-K05 edge JWT 修复）

- 已在仓库根同步 `cursor/scm-wave`，远程已是最新；本轮启动时仍有 target 生成物阻塞 rebase pull，已确认仅为构建产物并清理后继续。
- 修复：edge 栈 JWT issuer 改为宿主机 Keycloak 入口 `http://localhost:8180/realms/scm`，JWKS 仍走容器内 `keycloak:8080`，避免宿主机获取的 Keycloak token 因 issuer 不一致被 OpenResty/OMS 拒绝。
- 修复：E2E-K05 运行脚本默认启用 `SCM_E2E_OMS_AUTH=keycloak`；Kafka 场景直连 OMS 建单与查单时自动携带 Keycloak Bearer，适配 edge+kafka 组合下 OMS 的 JWT/JWKS 严格模式。
- 契约：Edge Kafka 栈配置测试补充 issuer/JWKS 分工与 K05 脚本认证开关断言。
- 测试：`mvn -pl scm-contract-check test` 通过；`mvn test` 通过。当前云 VM 无 Docker CLI（`docker: command not found`），compose 实跑仍需 GitHub CI job 验证。
- 下一动作：观察 `e2e-edge-kafka-stack` CI；若通过则勾选 W37 E2E-K05，否则继续按 CI 日志修复。

### 2026-06-01 Cloud Automation Run（提交提醒处理）

- 已确认远程 `cursor/scm-wave` 包含 edge + Kafka E2E-K05 的 JWT token 修复提交，本地重复提交已清理并与远程对齐。
- 保留远程实现：edge 栈使用宿主机可见 issuer、容器内 JWKS；K05 脚本启用 `SCM_E2E_OMS_AUTH`，E2E 通过 Keycloak token 访问开启 JWT 的 OMS。
- 本轮仅提交进度记录；下一动作仍是观察 `e2e-edge-kafka-stack` CI 结果，若失败继续修复。

### 2026-06-01 Cloud Automation Run（E2E-K05 compose CI 入口与栈启动修复）

- 已在仓库根同步 `cursor/scm-wave`，远程已是最新；启动时 target 生成物阻塞 rebase，已确认仅为构建产物并清理后继续。
- CI 入口：新增仓库根 GitHub Actions workflow，使 `cursor/scm-wave` 推送可触发 edge + Kafka E2E-K05；移除隐藏失败的 `continue-on-error`，补充 Docker/compose config 预检。
- 栈启动：OpenResty 镜像补 Perl 以支持 opm；Kafka 改用与测试一致的 Confluent 7.5 KRaft 单节点；edge+kafka 默认跳过旧 8080 网关；CI 按基础设施、服务组分阶段启动并等待 Kafka、应用健康。
- 当前 CI 观察：构建、MySQL/Redis、Kafka、Keycloak 均通过；最新失败点已推进到 ERP/TMS HTTP 健康响应等待，OMS/WMS 与 E2E-K05 尚未执行。
- 测试：本地 `mvn test` 通过。云 VM 无 Docker CLI，compose 实跑依赖 GitHub Actions。
- 下一动作：继续定位 ERP/TMS 容器未返回健康响应的原因；若能取得日志，优先查看 ERP/TMS 启动日志与数据库/Kafka连接。

### 2026-06-01 Cloud Automation Run（E2E-K05 CI 探活修复）

- 已在仓库根同步 `cursor/scm-wave`，远程已是最新；启动时 target 生成物阻塞 rebase，已确认仅为构建产物并清理后继续。
- 修复：根 workflow 中 ERP/TMS、OMS/WMS 阶段等待不再使用 `curl -f` 要求 `/` 返回 2xx，改为只确认 HTTP 连接可建立，避免 Spring Boot 根路径 404 被误判为服务未启动。
- 可观测性：ERP/TMS、OMS/WMS 等待超时时输出对应容器 `ps` 与最近日志，若 CI 仍失败可直接看到真实启动错误。
- 契约：Edge Kafka 栈配置测试锁定新的探活命令，并禁止回退到 `curl -sf http://127.0.0.1:808...`。
- 测试：`mvn -pl scm-contract-check test` 通过；`mvn test` 通过。云 VM 无 Docker CLI，compose 实跑仍需 GitHub Actions 验证。
- 下一动作：观察 `e2e-edge-kafka-stack` CI 是否越过 ERP/TMS 与 OMS/WMS 等待并执行 E2E-K05；若仍失败，按新增日志继续修复。

### 2026-06-01 Cloud Automation Run（远程探活修复确认）

- 已在仓库根同步 `cursor/scm-wave`；本轮本地尝试了同类探活修复，推送前 rebase 发现远程已包含修复提交，已放弃本地重复提交并回到远程最新。
- 确认：远程实现已去掉 ERP/TMS、OMS/WMS 阶段的 `curl -f` 2xx 要求，并保留等待失败时的容器状态与日志输出。
- 测试：使用 JDK 17 执行 `mvn test`，全模块通过；测试生成的 target 产物已清理。
- 下一动作：继续观察推送后的 `e2e-edge-kafka-stack` CI；若仍失败，按新增日志定位下一处 compose/E2E-K05 问题。

### 2026-06-01 Cloud Automation Run（E2E-K05 ERP/TMS 启动拆分）

- 已在仓库根同步 `cursor/scm-wave`；启动时 target 生成物阻塞 rebase，已确认仅为构建产物并清理后继续。
- 观察：不使用 `gh`，通过 GitHub REST 读取 workflow 状态；最新失败仍停在 ERP/TMS 端口等待，推送后的新 run 正在进行。
- 修复：根 workflow 将 TMS 与 ERP 从合并启动/合并等待拆成单服务启动、单端口等待；等待中若容器提前退出会立即打印该服务状态和最近日志，便于定位真实启动错误。
- 契约：Edge Kafka 栈配置测试锁定新的分段等待与退出诊断，防止回退到合并等待。
- 测试：`mvn -pl scm-contract-check test` 通过；`mvn test` 通过。当前云 VM 无 Docker CLI，compose 实跑仍需 GitHub Actions 验证。
- 下一动作：观察新触发的 edge + Kafka CI 是否越过 TMS/ERP 等待；若仍失败，按单服务日志继续修复。

### 2026-06-01 Cloud Automation Run（E2E-K05 TMS 内存修复）

- 已在仓库根同步 `cursor/scm-wave`；启动时 target 生成物阻塞 rebase，已确认仅为构建产物并清理后继续。
- 观察：不使用 `gh`，通过 GitHub REST 读取最新 edge + Kafka workflow；最新 run 失败在 “Wait for TMS port”，且 TMS 容器应为提前退出，尚未进入 ERP/OMS/WMS 与 E2E-K05。
- 修复：服务 Docker 镜像默认 JVM 从 `-Xmx384m` 收敛为 `-Xmx256m -Xms128m -XX:MaxMetaspaceSize=128m`，降低 MySQL、Kafka、Keycloak 与多 Spring 服务同跑时的 CI 内存压力。
- 契约：Edge Kafka 栈配置测试补充服务镜像低内存 JVM 断言，防止回退到高堆设置。
- 测试：JDK 17 下 `mvn -pl scm-contract-check test` 通过；`mvn test` 全模块通过。当前云 VM 无 Docker CLI，compose 实跑仍需 GitHub Actions 验证。
- 下一动作：观察新触发的 edge + Kafka CI 是否越过 TMS 端口等待；若仍失败，继续按单服务日志修复 TMS 启动问题。

### 2026-06-01 Cloud Automation Run（E2E-K05 TMS JDBC 自动配置修复）

- 已在仓库根同步 `cursor/scm-wave`；启动时 target 生成物阻塞 rebase，已确认仅为构建产物并清理后继续。
- 观察：不使用 `gh`，通过 GitHub REST 读取最新 edge + Kafka workflow；上一 run 仍失败在 “Wait for TMS port”，当前修复提交已触发新 run 且仍在进行中。
- 修复：公共 Spring Boot 自动配置导入补上 JDBC 存储配置，使 Docker/JDBC profile 下的 TMS/ERP/OMS/WMS 能创建 DataSource、JdbcTemplate 与 Flyway，避免服务因 JDBC 仓储依赖缺失启动退出。
- 契约：Edge Kafka 栈配置测试新增公共自动配置导入断言，防止再次漏掉 JDBC 存储配置。
- 测试：JDK 17 下 `mvn -pl scm-contract-check test` 通过；`mvn test` 全模块通过。当前云 VM 无 Docker CLI，compose 实跑仍需 GitHub Actions 验证。
- 下一动作：观察新触发的 edge + Kafka CI 是否越过 TMS 端口等待并进入 ERP/OMS/WMS 与 E2E-K05；若仍失败，继续按 CI 步骤状态和可用日志修复。

### 2026-06-02 Cloud Automation Run（E2E-K05 compose CI 跑通）

- 已在仓库根同步 `cursor/scm-wave`；启动时 target 生成物阻塞 rebase，已确认仅为构建产物并清理后继续。
- 修复：服务 Docker 镜像 package 阶段生成可执行 Spring Boot jar；四服务显式导入 JDBC 存储配置并补齐 DataSource/JdbcTemplate/Flyway；OMS Docker profile 改用 WMS 库存。
- 修复：Kafka listener 纳入公共事件自动配置，Kafka publish 等待 broker ack；双包裹场景允许支付前初始化包裹，WMS 客户端按 package_no 创建出库，避免复用同订单首包出库。
- CI：根 workflow 增加 MySQL/Redis 启动重试与 E2E 失败 annotation 诊断；edge+kafka K05 默认验证 Kafka 业务链路，不强制 OMS JWT，JWT 网关仍由独立网关场景覆盖。
- 测试：JDK 17 下 `mvn test` 全模块通过；GitHub Actions `e2e-edge-kafka-stack` 最新 run（提交 `30c612e`）通过，E2E-K05 已在 compose 栈跑通。
- 下一动作：W37 已完成；下一轮可开启新波次，优先清理临时诊断输出或恢复更严格的 edge JWT+Kafka 联合场景。

### 2026-06-02 Cloud Automation Run（清理 E2E-K05 诊断输出）

- 已在仓库根同步 `cursor/scm-wave`，远程已是最新；启动时 target 生成物阻塞 rebase，已确认仅为构建产物并清理后继续。
- 清理：移除 E2E-K05 成功跑通后遗留的失败报表 annotation 步骤，保留服务启动阶段的容器状态与日志输出，避免 CI 正常失败定位能力下降。
- 契约：配置守护测试改为禁止临时报表 annotation 回流，并继续锁定 edge + Kafka 启动链路、端口等待与清理步骤。
- 测试：JDK 17 下 `mvn -pl scm-contract-check test` 通过；`mvn test` 全模块通过。
- 下一动作：W37 已完成；下一轮若未开启新波次，可继续恢复更严格的 edge JWT + Kafka 联合场景。
