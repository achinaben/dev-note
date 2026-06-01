# language: zh-CN
@e2e @scm
功能: 供应链四系统端到端

  背景:
    假定 测试夹具已加载
    并且 各服务健康检查通过

  @E2E-01 @smoke
  场景: B2C 下单支付发货签收
    当 用户提交订单 使用夹具 client_token
    那么 订单状态应为 CREATED
    当 通过 mock-pay 触发支付成功
    那么 订单状态应为 PAID
    并且 库存已 Confirm
    并且 远程库存已 Confirm
    并且 消息 ORDER_PAID 已发布
    当 等待 WMS 出库单创建并发运
    那么 订单状态应为 SHIPPED
    并且 消息 WMS_OUTBOUND_SHIPPED 已被 ERP 消费
    并且 ERP 存在凭证 je_no
    当 模拟 TMS 签收
    那么 订单状态应为 DELIVERED

  @E2E-02
  场景: 建单幂等
    当 用户连续两次提交订单 相同 client_token
    那么 两次返回的 order_no 应相同
    并且 数据库仅 1 条 trade_order

  @E2E-03
  场景: 支付回调幂等
    假如 已 CREATED 订单
    当 支付回调重复 3 次 相同 notify_id
    那么 订单状态应为 PAID
    并且 order_payment 仅 1 条成功记录

  @E2E-04
  场景: 超时关单
    假如 已 CREATED 订单 且 支付已过期
    当 触发关单任务
    那么 订单状态应为 CLOSED
    并且 库存已 Release

  @E2E-05
  场景: 部分发货
    假如 已 PAID 订单 含 2 个包裹
    当 仅第 1 个包裹 WMS 发运
    那么 订单状态应为 PARTIAL_SHIPPED
    当 第 2 个包裹发运
    那么 订单状态应为 SHIPPED

  @E2E-06
  场景: 退货退款
    假如 已 SHIPPED 订单
    当 用户申请退货退款 并通过审核
    并且 WMS 退货入库完成
    当 模拟退款成功
    那么 售后状态应为 REFUND_SUCCESS
    并且 消息 REFUND_COMPLETED 已被 ERP 消费

  @E2E-07
  场景: WMS 出库幂等
    当 使用相同 package_no 两次创建出库单
    那么 仅 1 张 outbound_order
    并且 第二次返回码为 WMS_10001 或 HTTP 409

  @E2E-08
  场景: 轨迹乱序不回退
    假如 订单已 SHIPPED
    当 先推送 TMS_DELIVERED
    并且 后推送 TMS_IN_TRANSIT
    那么 订单状态应保持 DELIVERED

  @E2E-09
  场景: ERP 出库过账幂等
    当 相同 biz_key 的 WMS_OUTBOUND_SHIPPED 消费 2 次
    那么 journal_entry 仅 1 张

  @E2E-10
  场景: B2B 信用拒绝
    当 B2B 下单金额超过信用额度
    那么 应返回 ERP_03001 或 OMS 映射的业务错误

  @E2E-11 @smoke
  场景: mock-carrier 轨迹闭环签收
    当 用户提交订单 使用夹具 client_token
    当 通过 mock-pay 触发支付成功
    当 等待 WMS 出库单创建并发运
    那么 订单状态应为 SHIPPED
    当 通过 mock-carrier 触发签收轨迹
    那么 订单状态应为 DELIVERED
