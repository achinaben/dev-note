# language: zh-CN
@e2e @jdbc @strict-outbound
功能: jdbc + 严格出库 pick → check → handover（需 start-all -Jdbc -WmsStrict）

  背景:
    假定 测试夹具已加载
    并且 各服务健康检查通过

  @E2E-14
  场景: jdbc 严格出库与 TMS 运单联动
    当 用户提交订单 使用夹具 client_token
    当 通过 mock-pay 触发支付成功
    并且 消息 ORDER_PAID 已发布
    当 等待 WMS 出库单由事件创建
    当 WMS 对出库单执行拣货
    并且 WMS 对出库单执行复核
    当 WMS 对出库单执行交接发运
    那么 WMS 出库单状态应为 SHIPPED
    并且 TMS 运单号应与 WMS 交接运单号一致
    并且 TMS 存在 WMS_HANDOVER 轨迹事件
    并且 消息 WMS_OUTBOUND_SHIPPED 已被 ERP 消费
    并且 ERP 凭证应包含交接运单号
    当 通过 mock-carrier 用交接运单号触发签收
    那么 订单状态应为 DELIVERED
