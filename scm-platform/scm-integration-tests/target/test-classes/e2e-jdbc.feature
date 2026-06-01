# language: zh-CN
@e2e @jdbc
功能: MySQL jdbc 存储全链路冒烟（需 start-all -Jdbc）

  背景:
    假定 测试夹具已加载
    并且 各服务健康检查通过

  @E2E-J01
  场景: jdbc 下建单幂等与 trade_order 计数
    当 用户连续两次提交订单 相同 client_token
    那么 两次返回的 order_no 应相同
    并且 数据库仅 1 条 trade_order

  @E2E-J02 @smoke
  场景: jdbc B2C 下单支付发货
    当 用户提交订单 使用夹具 client_token
    当 通过 mock-pay 触发支付成功
    那么 订单状态应为 PAID
    当 等待 WMS 出库单创建并发运
    那么 订单状态应为 SHIPPED
    并且 TMS 已创建运单

  @E2E-J03
  场景: jdbc WMS 出库幂等
    当 使用相同 package_no 两次创建出库单
    那么 仅 1 张 outbound_order
    并且 第二次返回码为 WMS_10001 或 HTTP 409

  @E2E-J04
  场景: jdbc 轨迹乱序不回退
    假如 订单已 SHIPPED
    当 先推送 TMS_DELIVERED
    并且 后推送 TMS_IN_TRANSIT
    那么 订单状态应保持 DELIVERED

  @E2E-J05
  场景: jdbc ERP 过账幂等
    当 相同 biz_key 的 WMS_OUTBOUND_SHIPPED 消费 2 次
    那么 journal_entry 仅 1 张

  @E2E-J06
  场景: jdbc B2B 信用拒绝
    当 B2B 下单金额超过信用额度
    那么 应返回 ERP_03001 或 OMS 映射的业务错误

  @E2E-J08
  场景: jdbc mock-carrier 轨迹签收
    当 用户提交订单 使用夹具 client_token
    当 通过 mock-pay 触发支付成功
    当 等待 WMS 出库单创建并发运
    那么 订单状态应为 SHIPPED
    当 通过 mock-carrier 触发签收轨迹
    那么 订单状态应为 DELIVERED

  @E2E-J07
  场景: jdbc 退货退款
    假如 已 SHIPPED 订单
    当 用户申请退货退款 并通过审核
    并且 WMS 退货入库完成
    当 模拟退款成功
    那么 售后状态应为 REFUND_SUCCESS
    并且 消息 REFUND_COMPLETED 已被 ERP 消费
