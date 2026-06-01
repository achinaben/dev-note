# language: zh-CN
@e2e @jwt @smoke
功能: OMS JWT scope 建单（需 OMS profile jwt）

  背景:
    假定 测试夹具已加载
    并且 各服务健康检查通过

  @E2E-JWT01
  场景: 带 oms.write scope 的 JWT 建单
    当 使用 JWT 提交订单 oms.write
    那么 订单状态应为 CREATED

  @E2E-JWT02
  场景: Keycloak 真实 token 建单（需 Keycloak 8180）
    当 使用 Keycloak token 提交订单
    那么 订单状态应为 CREATED
