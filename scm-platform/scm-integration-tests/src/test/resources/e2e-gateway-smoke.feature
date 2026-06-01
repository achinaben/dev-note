# language: zh-CN
@e2e @gateway @smoke
功能: 网关 smoke（可选 SCM_GATEWAY_URL，默认直连 OMS）

  背景:
    假定 测试夹具已加载
    并且 各服务健康检查通过

  @E2E-GW01
  场景: 经网关或直连提交订单 smoke
    当 用户经网关提交订单 使用夹具 client_token
    那么 订单状态应为 CREATED

  @E2E-GW02
  场景: 经网关带 JWT 建单（需 SCM_GATEWAY_URL + OMS jwt）
    当 使用 JWT 经网关提交订单 oms.write
    那么 订单状态应为 CREATED

  @E2E-GW03
  场景: 经网关 JWT scope 不足被拒绝（需 OpenResty JWT 网关）
    假定 JWT 网关地址已配置
    当 使用错误 scope 的 JWT 经网关提交订单
    那么 HTTP 响应状态码应为 403

  @E2E-MTLS01
  场景: 经网关缺少 API Key 被拒绝（南北向鉴权 smoke）
    假定 网关地址已配置
    当 经网关不带 API Key 提交订单
    那么 HTTP 响应状态码应为 401

  @E2E-GW04
  场景: 经 JWT 网关无 Bearer 建单被拒（需 SCM_GATEWAY_JWT_URL + OMS jwt）
    假定 JWT 网关地址已配置
    当 经 JWT 网关不带 token 提交订单
    那么 HTTP 响应状态码应为 401
