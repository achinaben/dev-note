# language: zh-CN
@e2e @wms-inventory
功能: OMS 使用 WMS 库存分配（替代 mock-inventory）

  背景:
    假定 测试夹具已加载
    并且 各服务健康检查通过

  @E2E-12
  场景: WMS 库存预占与确认
    当 用户提交订单 使用夹具 client_token
    当 通过 mock-pay 触发支付成功
    那么 订单状态应为 PAID
    并且 库存已 Confirm
    并且 WMS库存已 Confirm
