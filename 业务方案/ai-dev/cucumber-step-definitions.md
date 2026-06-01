# Cucumber Step 实现模板（Java + RestAssured）

> AI 在 `scm-integration-tests` 中实现 `E2ESteps`，端口见 README。

```java
public class E2ESteps {
  @Given("测试夹具已加载")
  public void loadFixtures() { /* 调 seed API 或执行 W1_master_data.sql */ }

  @When("用户提交订单 使用夹具 client_token")
  public void submitOrder() {
    given().header("Idempotency-Key", "ct-fix-001")
      .body(loadJson("create-order.json"))
      .post(omsBase + "/api/v1/orders");
  }

  @When("模拟支付成功回调")
  public void payNotify() {
    post(mockPay + "/trigger", Map.of("out_trade_no", lastPaymentNo));
  }

  @Then("订单状态应为 {string}")
  public void assertOrderStatus(String status) {
    await().until(() -> getOrder().status.equals(status));
  }

  @When("等待 WMS 出库单创建并发运")
  public void wmsShip() {
    await().until(() -> wmsHasShipped(lastPackageNo));
  }

  @Then("ERP 存在凭证 je_no")
  public void assertJe() {
    assertThat(erpClient.getJeBySource(lastOutboundNo)).isNotNull();
  }
}
```

**create-order.json** 字段必须与 `fixtures.yaml` 一致。
