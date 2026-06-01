# 单元测试与集成测试目录（AI 逐项实现并勾选）



> 命名：`{Class}Test` 单测，`{Class}IT` 需 Testcontainers。



## scm-common



- [x] `EventEnvelopeJsonTest` — 校验 event-envelope.schema.json

- [x] `ProcessedMessageRepositoryTest`（含于 EventEnvelopeJsonTest）

- [x] `OutboxPollerIT` — Kafka + MySQL（`mvn -Pdocker-it`）

- [x] `OrgIdContextTest` — W19 多租户上下文



## scm-common-spring



- [x] `MemoryEventBusTest`
- [x] `JwtClaimsValidatorTest` — claims / JWKS 模式（W24）



## scm-oms-service



- [x] `OrderStateMachineTest` — 第二十一章全表

- [x] `CreateOrderIdempotentTest`

- [x] `CreateOrderContractTest` — openapi-oms-core POST /orders

- [x] `PaymentNotifyIdempotentTest`

- [ ] `PaymentNotifyHandlerIT`

- [ ] `SplitOrderServiceTest`

- [x] `InventoryRemoteWireMockTest` — mock 远程 reserve/confirm
- [x] `InventoryWmsProviderTest` — provider=wms（W21）

- [x] `WmsOutboundShippedHandlerTest` — OMS 消费 WMS_OUTBOUND_SHIPPED

- [x] `TmsTrackRankTest` — E2E-08 轨迹 rank 不回退

- [x] `TmsTrackContractTest` — TMS 轨迹契约

- [ ] `AftersaleSagaTest`

- [x] `OpsDisabledTest` — prod 关闭 ops

- [x] `OpsApiKeyTest` — X-Ops-Token
- [x] `JwtScopeAccessTest` — Bearer scope oms.write（W23）

- [x] `OrgIdPassthroughTest` — X-Org-Id

- [ ] `OmsOrderJdbcIT` — `mvn -Pdocker-it`



## scm-wms-service



- [x] `OutboundStateMachineTest` — W23 CREATED→PICKED→CHECKED→SHIPPED

- [x] `OutboundIdempotentTest` — E2E-07
- [x] `CreateOutboundContractTest` — openapi-wms-core

- [x] `OutboundStoreTest`

- [x] `OutboundShipIT` — 发运调 ERP

- [x] `InventoryAllocationContractTest` — openapi-inventory /reserve（W21）
- [x] `AllocationServiceFefoTest` — FEFO 效期优先（W22）

- [x] `AllocationConcurrentTest` — 并发预占无负库存（W22）

- [x] `WmsInventoryJdbcIT` — 库存 jdbc 持久化（`mvn -Pdocker-it`）

- [x] `PickConfirmIdempotentTest` — RF operation_id 幂等（W24）
- [x] `RfPickConfirmContractTest` — openapi rf pick confirm（W24）

- [x] `ShipHandoverPublishesEventTest` — WMS_SHIP_HANDOVER + WMS_OUTBOUND_SHIPPED（W23）

- [ ] `FulfillmentReleasedConsumerIT`

- [ ] `WmsOutboundJdbcIT` — `mvn -Pdocker-it`



## scm-tms-service



- [x] `FreightEstimateMockCarrierTest` — mock-carrier 报价（原 FreightEstimateTest）

- [x] `FreightEstimateContractTest` — openapi 运费

- [x] `FreightRoutingTest` — recommended 择优

- [ ] `RoutingRuleEngineTest`

- [x] `ShipmentIdempotentTest` — 运单幂等

- [x] `ShipmentStoreTest`

- [x] `CreateShipmentContractTest`

- [x] `CreateShipmentCarrierTest` — recommended 承运商

- [x] `ShipmentInterceptTest`

- [x] `CarrierCallbackWireMockTest`

- [x] `CarrierCallbackContractTest`

- [ ] `TrackingDedupTest`

- [ ] `CarrierCircuitBreakerTest`

- [ ] `DeliveredPublisherIT`

- [ ] `TmsShipmentJdbcIT` — `mvn -Pdocker-it`



## scm-erp-service



- [x] `PostingEngineTest`

- [x] `CreditCheckContractTest` — E2E-10 信用契约（POST /credit/check）

- [x] `WmsShipmentPostingIT` — E2E-09 幂等
- [x] `WmsShipmentPostContractTest` — POST /integration/wms/shipment

- [x] `MaterialControllerTest`

- [x] `OrgIdPostingTest` — WMS→ERP org 透传

- [ ] `FiscalPeriodCloseGuardTest`

- [x] `JournalBalanceTest`（含于 PostingEngineTest 借贷平）



## scm-contract-check



- [x] `OpenApiContractDriftTest` — W15 契约漂移



## scm-mock-carrier



- [x] `QuoteControllerTest`



## scm-integration-tests



- [x] `RunCucumberE2E` — 10 场景（`-Pe2e`，需四服务 + mock 侧车）

- [x] `RunCucumberKafkaE2E` — E2E-K01~K11（`-Pe2e-kafka`）
- [x] `RunCucumberWmsInventoryE2E` — E2E-12（`-Pe2e-wms-inventory`，需 `-WmsInventory`）
- [x] `RunCucumberStrictOutboundE2E` — E2E-13（`-Pe2e-strict-outbound`，需 `-WmsStrict`）

- [x] `RunCucumberJdbcE2E` — E2E-J01~J08（`-Pe2e-jdbc`）

- [x] `E2EStepDefinitions` — 含远程库存 Confirm（W20）

- [x] `B2CLocalFlowIT` — 本地四服务 + mock-inventory 冒烟

- [x] `CucumberSmokeTest` — 默认 test 占位



**退出（W6/W11）**：`mvn test` 绿 + `scripts/start-all` + `mvn -pl scm-integration-tests -Pe2e test` 绿。



**CI（W12/W16）**：GitHub Actions `scm-ci.yml`（unit + e2e + e2e-kafka + e2e-jdbc + docker-it）。



**波次文档**：`scm-platform/docs/WAVES_W15_W20.md`；覆盖率汇总 `scripts/coverage-report.ps1`。


