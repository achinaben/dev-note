# language: zh-CN
@e2e @jwt @jwks
功能: OMS Keycloak RS256 验签建单（需 Keycloak 8180 + OMS profiles jwt,jwt-jwks）

  背景:
    假定 测试夹具已加载
    并且 各服务健康检查通过

  @E2E-JWT02
  场景: Keycloak 真实 token 建单（RS256 验签）
    当 使用 Keycloak token 提交订单
    那么 订单状态应为 CREATED
