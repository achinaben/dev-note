# language: zh-CN
@e2e @kafka
功能: Kafka 事件驱动子集（不依赖 OMS ops /ship）

  背景:
    假定 测试夹具已加载
    并且 各服务健康检查通过

  @E2E-K01
  场景: 支付后 ORDER_PAID 驱动 WMS 建出库
    当 用户提交订单 使用夹具 client_token
    当 通过 mock-pay 触发支付成功
    那么 订单状态应为 PAID
    并且 消息 ORDER_PAID 已发布
    当 等待 WMS 出库单由事件创建
    那么 WMS 存在出库单

  @E2E-K02
  场景: WMS 发运经 Kafka 触发 ERP 过账
    假如 已 PAID 且 WMS 已建出库
    当 对 WMS 出库单执行发运
    并且 消息 WMS_OUTBOUND_SHIPPED 已被 ERP 消费

  @E2E-K03
  场景: Kafka 发运后 OMS 订单变 SHIPPED
    假如 已 PAID 且 WMS 已建出库
    当 对 WMS 出库单执行发运
    那么 订单状态应为 SHIPPED

  @E2E-K04
  场景: Kafka 发运后 OMS 自动 TMS 下单
    假如 已 PAID 且 WMS 已建出库
    当 对 WMS 出库单执行发运
    那么 订单状态应为 SHIPPED
    并且 TMS 已创建运单

  @E2E-K05
  场景: Kafka 双包裹部分发运
    假如 已 PAID 且含 2 个包裹 Kafka
    当 对第 1 个包裹的 WMS 出库单执行发运
    那么 订单状态应为 PARTIAL_SHIPPED
    当 对第 2 个包裹的 WMS 出库单执行发运
    那么 订单状态应为 SHIPPED

  @E2E-K06
  场景: WMS 出库幂等
    当 使用相同 package_no 两次创建出库单
    那么 仅 1 张 outbound_order
    并且 第二次返回码为 WMS_10001 或 HTTP 409

  @E2E-K07
  场景: 轨迹乱序不回退
    假如 订单已 SHIPPED
    当 先推送 TMS_DELIVERED
    并且 后推送 TMS_IN_TRANSIT
    那么 订单状态应保持 DELIVERED

  @E2E-K08
  场景: ERP 出库过账幂等
    当 相同 biz_key 的 WMS_OUTBOUND_SHIPPED 消费 2 次
    那么 journal_entry 仅 1 张

  @E2E-K09
  场景: B2B 信用拒绝
    当 B2B 下单金额超过信用额度
    那么 应返回 ERP_03001 或 OMS 映射的业务错误

  @E2E-K11
  场景: mock-carrier 轨迹闭环签收
    当 用户提交订单 使用夹具 client_token
    当 通过 mock-pay 触发支付成功
    当 等待 WMS 出库单创建并发运
    那么 订单状态应为 SHIPPED
    当 通过 mock-carrier 触发签收轨迹
    那么 订单状态应为 DELIVERED

  @E2E-K10
  场景: 退货退款
    假如 已 SHIPPED 订单
    当 用户申请退货退款 并通过审核
    并且 WMS 退货入库完成
    当 模拟退款成功
    那么 售后状态应为 REFUND_SUCCESS
    并且 消息 REFUND_COMPLETED 已被 ERP 消费
