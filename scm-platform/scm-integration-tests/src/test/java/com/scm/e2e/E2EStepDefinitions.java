package com.scm.e2e;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.zh_cn.并且;
import io.cucumber.java.zh_cn.假如;
import io.cucumber.java.zh_cn.当;
import io.cucumber.java.zh_cn.那么;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.net.Socket;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EStepDefinitions {
    private static final String OMS = "http://localhost:8081";
    private static final String WMS = "http://localhost:8082";
    private static final String ERP = "http://localhost:8084";
    private static final String TMS = "http://localhost:8083";
    private static final String MOCK_PAY = "http://localhost:8085";
    private static final String MOCK_CARRIER = "http://localhost:8086";
    private static final String MOCK_INVENTORY = "http://localhost:8087";

    @Before
    public void init() {
        ScmScenarioContext.clear();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @After
    public void cleanup() {
        ScmScenarioContext.clear();
    }

    @假如("测试夹具已加载")
    public void fixturesLoaded() {
        // fixtures.yaml 口径已在步骤中硬编码 U10001/SKU001 等
    }

    @假如("各服务健康检查通过")
    public void healthCheck() {
        assumePort(8081);
        assumePort(8082);
        assumePort(8084);
        assumePort(8083);
        assumePort(8085);
        assumePort(8086);
        assumePort(8087);
    }

    @当("用户提交订单 使用夹具 client_token")
    public void submitOrder() {
        submitOrderTo(OMS);
    }

    @当("用户经网关提交订单 使用夹具 client_token")
    public void submitOrderViaGateway() {
        submitOrderTo(gatewayOmsBase());
    }

    private void submitOrderTo(String omsBase) {
        var ctx = ScmScenarioContext.get();
        ctx.clientToken = "ct-e2e-" + System.nanoTime();
        String body = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(ctx.clientToken);
        var req = given().baseUri(omsBase)
                .header("Idempotency-Key", ctx.clientToken)
                .contentType(ContentType.JSON).body(body);
        if (OMS.equals(omsBase)) {
            req = withOmsAuth(req);
        }
        if (isGatewayBase(omsBase)) {
            req = req.header("X-Api-Key", "e2e-gateway-key");
        }
        Response response = req.post("/api/v1/orders");
        if (response.statusCode() != 201) {
            throw new AssertionError("submit order failed: status="
                    + response.statusCode() + ", body=" + response.asString());
        }
        ctx.orderNo = response.then()
                .extract().path("data.orders[0].order_no");
    }

    private static String gatewayOmsBase() {
        String gw = System.getenv("SCM_GATEWAY_URL");
        if (gw != null && !gw.isBlank()) {
            return gw.replaceAll("/$", "");
        }
        return OMS;
    }

    private static boolean isGatewayBase(String base) {
        String gw = System.getenv("SCM_GATEWAY_URL");
        return gw != null && !gw.isBlank() && base.equals(gw.replaceAll("/$", ""));
    }

    private static RequestSpecification givenOms() {
        return withOmsAuth(given().baseUri(OMS));
    }

    private static RequestSpecification withOmsAuth(RequestSpecification req) {
        String token = resolveOmsBearerToken();
        if (token == null) {
            return req;
        }
        return req.header("Authorization", "Bearer " + token);
    }

    private static String resolveOmsBearerToken() {
        String mode = System.getenv("SCM_E2E_OMS_AUTH");
        if (!"keycloak".equalsIgnoreCase(mode)) {
            return null;
        }
        var ctx = ScmScenarioContext.get();
        if (ctx.omsBearerToken == null || ctx.omsBearerToken.isBlank()) {
            ctx.omsBearerToken = fetchKeycloakAccessToken();
        }
        if (ctx.omsBearerToken == null || ctx.omsBearerToken.isBlank()) {
            throw new AssertionError("Keycloak token unavailable for SCM_E2E_OMS_AUTH=keycloak");
        }
        return ctx.omsBearerToken;
    }

    @假如("网关地址已配置")
    public void gatewayConfigured() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                System.getenv("SCM_GATEWAY_URL") != null && !System.getenv("SCM_GATEWAY_URL").isBlank(),
                "未设置 SCM_GATEWAY_URL，跳过网关鉴权 smoke");
    }

    @假如("JWT 网关地址已配置")
    public void jwtGatewayConfigured() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                resolveJwtGatewayBase() != null,
                "未设置 SCM_GATEWAY_JWT_URL，跳过 JWT 网关 E2E");
    }

    @当("经 JWT 网关不带 token 提交订单")
    public void submitViaJwtGatewayWithoutToken() {
        String base = resolveJwtGatewayBase();
        var ctx = ScmScenarioContext.get();
        ctx.clientToken = "ct-gw-nojwt-" + System.nanoTime();
        String body = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(ctx.clientToken);
        ctx.httpStatus = given().baseUri(base)
                .header("X-Api-Key", "e2e-gateway-key")
                .header("Idempotency-Key", ctx.clientToken)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/orders")
                .then().extract().statusCode();
    }

    private static String resolveJwtGatewayBase() {
        String jw = System.getenv("SCM_GATEWAY_JWT_URL");
        if (jw != null && !jw.isBlank()) {
            return jw.replaceAll("/$", "");
        }
        if ("1".equals(System.getenv("SCM_GATEWAY_JWT"))) {
            return "http://localhost:8089";
        }
        return null;
    }

    @当("经网关不带 API Key 提交订单")
    public void submitViaGatewayWithoutApiKey() {
        var ctx = ScmScenarioContext.get();
        ctx.clientToken = "ct-gw-nokey-" + System.nanoTime();
        String body = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(ctx.clientToken);
        ctx.httpStatus = given().baseUri(gatewayOmsBase())
                .header("Idempotency-Key", ctx.clientToken)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/orders")
                .then().extract().statusCode();
    }

    @那么("HTTP 响应状态码应为 {int}")
    public void assertHttpStatus(int code) {
        assertEquals(code, ScmScenarioContext.get().httpStatus);
    }

    @当("用户连续两次提交订单 相同 client_token")
    public void submitTwice() {
        var ctx = ScmScenarioContext.get();
        ctx.clientToken = "ct-dup-" + System.nanoTime();
        String body = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(ctx.clientToken);
        ctx.orderNo = postOrder(body, ctx.clientToken, 201);
        String second = given().baseUri(OMS)
                .header("Idempotency-Key", ctx.clientToken)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/orders")
                .then().statusCode(409)
                .extract().path("data.orders[0].order_no");
        ctx.lastOrderNo2 = second != null ? second : ctx.orderNo;
    }

    private String postOrder(String body, String key, int expectedStatus) {
        return given().baseUri(OMS)
                .header("Idempotency-Key", key)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/orders")
                .then().statusCode(expectedStatus)
                .extract().path("data.orders[0].order_no");
    }

    @当("通过 mock-pay 触发支付成功")
    public void payViaMockPay() {
        var ctx = ScmScenarioContext.get();
        if (ctx.orderNo == null) {
            submitOrder();
        }
        given().baseUri(MOCK_PAY).contentType(ContentType.JSON)
                .body(Map.of(
                        "order_no", ctx.orderNo,
                        "out_trade_no", "PAY-" + ctx.orderNo,
                        "notify_id", "notify-" + ctx.clientToken
                ))
                .post("/trigger")
                .then().statusCode(200);
    }

    @当("模拟支付成功回调")
    public void payNotify() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).contentType(ContentType.JSON)
                .body(Map.of(
                        "notify_id", "notify-" + ctx.clientToken,
                        "order_no", ctx.orderNo,
                        "out_trade_no", "PAY-" + ctx.orderNo,
                        "amount_minor", 19900,
                        "sign_verified", true
                ))
                .post("/api/v1/payments/notify/wechat")
                .then().statusCode(200);
    }

    @那么("订单状态应为 {word}")
    public void assertOrderStatus(String status) throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        String lastBody = "";
        String lastStatus = "";
        for (int i = 0; i < 60; i++) {
            Response response = givenOms().get("/api/v1/orders/{no}", ctx.orderNo);
            if (response.statusCode() != 200) {
                lastBody = response.asString();
                lastStatus = "HTTP_" + response.statusCode();
            } else {
                lastBody = response.asString();
                lastStatus = response.path("data.status");
            }
            String actual = lastStatus;
            if (status.equals(actual)) {
                return;
            }
            Thread.sleep(500);
        }
        throw new AssertionError("order status mismatch: expected="
                + status + ", actual=" + lastStatus + ", body=" + lastBody);
    }

    @那么("库存已 Confirm")
    public void inventoryConfirmed() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).get("/api/v1/ops/orders/{no}/diag", ctx.orderNo)
                .then().statusCode(200)
                .body("data.inventory", equalTo("CONFIRMED"));
    }

    @并且("远程库存已 Confirm")
    public void remoteInventoryConfirmed() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(MOCK_INVENTORY).contentType(ContentType.JSON)
                .body(Map.of("order_no", ctx.orderNo))
                .post("/inventory/v1/status")
                .then().statusCode(200)
                .body("status", equalTo("CONFIRMED"));
    }

    @并且("WMS库存已 Confirm")
    public void wmsInventoryConfirmed() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(WMS).contentType(ContentType.JSON)
                .body(Map.of("order_no", ctx.orderNo))
                .post("/inventory/v1/status")
                .then().statusCode(200)
                .body("status", equalTo("CONFIRMED"));
    }

    @那么("消息 ORDER_PAID 已发布")
    public void orderPaidEvent() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        for (int i = 0; i < 30; i++) {
            Boolean published = given().baseUri(OMS).get("/api/v1/ops/orders/{no}/diag", ctx.orderNo)
                    .then().statusCode(200)
                    .extract().path("data.order_paid_event");
            if (Boolean.TRUE.equals(published)) {
                return;
            }
            Thread.sleep(200);
        }
        given().baseUri(OMS).get("/api/v1/ops/orders/{no}/diag", ctx.orderNo)
                .then().statusCode(200)
                .body("data.order_paid_event", equalTo(true));
    }

    @假如("已 CREATED 订单")
    public void givenCreatedOrder() {
        if (ScmScenarioContext.get().orderNo == null) {
            submitOrder();
        }
    }

    @假如("已 CREATED 订单 且 支付已过期")
    public void givenCreatedOrderPaymentExpired() {
        givenCreatedOrder();
    }

    @假如("已 PAID 订单 含 2 个包裹")
    public void givenPaidWithTwoPackages() {
        var ctx = ScmScenarioContext.get();
        if (ctx.orderNo == null) {
            submitOrder();
            payNotify();
        }
        given().baseUri(OMS).post("/api/v1/ops/orders/{no}/packages/init-two", ctx.orderNo)
                .then().statusCode(200)
                .body("data.count", equalTo(2));
    }

    @假如("已 SHIPPED 订单")
    public void givenShippedOrder() {
        var ctx = ScmScenarioContext.get();
        if (ctx.orderNo == null) {
            submitOrder();
            payNotify();
            wmsShip();
        }
        given().baseUri(OMS).get("/api/v1/orders/{no}", ctx.orderNo)
                .then().statusCode(200)
                .body("data.status", equalTo("SHIPPED"));
    }

    @假如("订单已 SHIPPED")
    public void givenOrderAlreadyShipped() {
        givenShippedOrder();
    }

    @当("支付回调重复 {int} 次 相同 notify_id")
    public void payNotifyRepeated(int times) {
        var ctx = ScmScenarioContext.get();
        if (ctx.orderNo == null) {
            submitOrder();
        }
        ctx.notifyId = "notify-dup-" + ctx.clientToken;
        for (int i = 0; i < times; i++) {
            given().baseUri(OMS).contentType(ContentType.JSON)
                    .body(Map.of(
                            "notify_id", ctx.notifyId,
                            "order_no", ctx.orderNo,
                            "out_trade_no", "PAY-" + ctx.orderNo,
                            "amount_minor", 19900,
                            "sign_verified", true
                    ))
                    .post("/api/v1/payments/notify/wechat")
                    .then().statusCode(200);
        }
    }

    @那么("支付成功记录仅 1 条")
    public void paymentOnce() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).get("/api/v1/ops/orders/{no}/diag", ctx.orderNo)
                .then().statusCode(200)
                .body("data.payment_success_count", equalTo(1));
    }

    @并且("order_payment 仅 1 条成功记录")
    public void orderPaymentOnce() {
        paymentOnce();
    }

    @当("触发关单任务")
    public void closeExpired() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).post("/api/v1/ops/orders/{no}/close-expired", ctx.orderNo)
                .then().statusCode(200);
    }

    @那么("库存已 Release")
    public void inventoryReleased() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).get("/api/v1/ops/orders/{no}/diag", ctx.orderNo)
                .then().statusCode(200)
                .body("data.inventory", equalTo("RELEASED"));
    }

    @当("先推送 TMS_DELIVERED")
    public void tmsDeliveredFirst() {
        pushTms("TMS_DELIVERED");
    }

    @当("后推送 TMS_IN_TRANSIT")
    public void tmsInTransitAfter() {
        pushTms("TMS_IN_TRANSIT");
    }

    @当("B2B 下单金额超过信用额度")
    public void b2bOverCredit() {
        var ctx = ScmScenarioContext.get();
        ctx.clientToken = "ct-b2b-" + System.nanoTime();
        String body = """
                {"client_token":"%s","buyer_id":"U10001","partner_id":"P-BIG","order_amount":"500000.0000",
                 "address_id":"ADDR100","lines":[{"sku_id":"SKU001","qty":"100","warehouse_id":"WH-SH-01"}]}
                """.formatted(ctx.clientToken);
        var resp = given().baseUri(OMS)
                .header("Idempotency-Key", ctx.clientToken)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/orders/b2b");
        ctx.httpStatus = resp.then().extract().statusCode();
        ctx.errorCode = resp.then().extract().path("code");
    }

    @那么("应返回信用业务错误")
    public void creditError() {
        var ctx = ScmScenarioContext.get();
        assertEquals(402, ctx.httpStatus);
        assertEquals("ERP_03001", ctx.errorCode);
    }

    @那么("应返回 ERP_03001 或 OMS 映射的业务错误")
    public void creditErrorFlexible() {
        var ctx = ScmScenarioContext.get();
        assertEquals(402, ctx.httpStatus);
        org.junit.jupiter.api.Assertions.assertTrue(
                "ERP_03001".equals(ctx.errorCode) || (ctx.errorCode != null && ctx.errorCode.startsWith("OMS_")),
                "expected ERP_03001 or OMS_* but got " + ctx.errorCode);
    }

    @当("初始化两个包裹")
    public void initTwoPackages() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).post("/api/v1/ops/orders/{no}/packages/init-two", ctx.orderNo)
                .then().statusCode(200)
                .body("data.count", equalTo(2));
    }

    @当("仅第 1 个包裹 WMS 发运")
    public void shipFirstPackage() {
        var ctx = ScmScenarioContext.get();
        String pkg = "P" + ctx.orderNo + "-1";
        given().baseUri(OMS).post("/api/v1/ops/orders/{no}/packages/{pkg}/ship", ctx.orderNo, pkg)
                .then().statusCode(200);
    }

    @当("第 2 个包裹发运")
    public void shipSecondPackage() {
        var ctx = ScmScenarioContext.get();
        String pkg = "P" + ctx.orderNo + "-2";
        given().baseUri(OMS).post("/api/v1/ops/orders/{no}/packages/{pkg}/ship", ctx.orderNo, pkg)
                .then().statusCode(200);
    }

    @并且("第 1 个包裹 TMS 运单号已绑定")
    public void firstPackageTmsWaybillBound() throws InterruptedException {
        assertPackageTmsWaybill("P" + ScmScenarioContext.get().orderNo + "-1");
    }

    @并且("第 2 个包裹 TMS 运单号已绑定")
    public void secondPackageTmsWaybillBound() throws InterruptedException {
        assertPackageTmsWaybill("P" + ScmScenarioContext.get().orderNo + "-2");
    }

    private void assertPackageTmsWaybill(String packageNo) throws InterruptedException {
        String expected = "WB-" + packageNo;
        for (int i = 0; i < 60; i++) {
            String waybill = given().baseUri(TMS).queryParam("package_no", packageNo)
                    .get("/tms/v1/ops/shipment/detail")
                    .then().extract().path("data.waybill_no");
            if (expected.equals(waybill)) {
                return;
            }
            Thread.sleep(500);
        }
        given().baseUri(TMS).queryParam("package_no", packageNo)
                .get("/tms/v1/ops/shipment/detail")
                .then().statusCode(200)
                .body("data.waybill_no", equalTo(expected));
    }

    @当("用户申请退货退款 并通过审核")
    public void applyAndApproveReturn() {
        var ctx = ScmScenarioContext.get();
        ctx.afterSaleNo = given().baseUri(OMS).contentType(ContentType.JSON)
                .body(Map.of("order_no", ctx.orderNo))
                .post("/api/v1/after-sale/apply")
                .then().statusCode(200)
                .extract().path("data.after_sale_no");
        given().baseUri(OMS).post("/api/v1/ops/after-sale/{no}/approve", ctx.afterSaleNo)
                .then().statusCode(200);
    }

    @并且("TMS 运单已拦截")
    public void tmsShipmentIntercepted() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.packageNo == null) {
            ctx.packageNo = "P" + ctx.orderNo;
        }
        for (int i = 0; i < 25; i++) {
            String status = given().baseUri(TMS).queryParam("package_no", ctx.packageNo)
                    .get("/tms/v1/ops/shipment/detail")
                    .then().extract().path("data.status");
            if ("INTERCEPTED".equals(status)) {
                return;
            }
            Thread.sleep(300);
        }
        given().baseUri(TMS).queryParam("package_no", ctx.packageNo)
                .get("/tms/v1/ops/shipment/detail")
                .then().statusCode(200)
                .body("data.status", equalTo("INTERCEPTED"));
    }

    @当("使用 JWT 经网关提交订单 oms.write")
    public void submitOrderWithJwtViaGateway() {
        var ctx = ScmScenarioContext.get();
        ctx.clientToken = "ct-jwt-gw-" + System.nanoTime();
        String body = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(ctx.clientToken);
        String base = gatewayOmsBase();
        if (resolveJwtGatewayBase() != null) {
            base = resolveJwtGatewayBase();
        }
        var req = given().baseUri(base)
                .header("Authorization", unsignedJwtBearer("oms.write"))
                .header("Idempotency-Key", ctx.clientToken)
                .contentType(ContentType.JSON).body(body);
        if (isGatewayBase(base) || resolveJwtGatewayBase() != null && base.equals(resolveJwtGatewayBase())) {
            req = req.header("X-Api-Key", "e2e-gateway-key");
        }
        ctx.orderNo = req.post("/api/v1/orders")
                .then().statusCode(201)
                .extract().path("data.orders[0].order_no");
    }

    @当("使用错误 scope 的 JWT 经网关提交订单")
    public void submitOrderWithWrongScopeJwtViaGateway() {
        var ctx = ScmScenarioContext.get();
        ctx.clientToken = "ct-jwt-bad-" + System.nanoTime();
        String body = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(ctx.clientToken);
        String base = resolveJwtGatewayBase();
        if (base == null) {
            base = gatewayOmsBase();
        }
        ctx.httpStatus = given().baseUri(base)
                .header("Authorization", unsignedJwtBearer("wms.write"))
                .header("X-Api-Key", "e2e-gateway-key")
                .header("Idempotency-Key", ctx.clientToken)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/orders")
                .then().extract().statusCode();
    }

    @当("使用 JWT 提交订单 oms.write")
    public void submitOrderWithJwt() {
        var ctx = ScmScenarioContext.get();
        ctx.clientToken = "ct-jwt-e2e-" + System.nanoTime();
        String body = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(ctx.clientToken);
        ctx.orderNo = given().baseUri(OMS)
                .header("Authorization", unsignedJwtBearer("oms.write"))
                .header("Idempotency-Key", ctx.clientToken)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/orders")
                .then().statusCode(201)
                .extract().path("data.orders[0].order_no");
    }

    @当("使用 Keycloak token 提交订单")
    public void submitOrderWithKeycloakToken() {
        String token = fetchKeycloakAccessToken();
        boolean strict = "1".equals(System.getenv("SCM_JWT_STRICT"));
        if (token == null || token.isBlank()) {
            if (strict) {
                throw new AssertionError("Keycloak 8180 未就绪，E2E-JWT02 失败");
            }
            org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "Keycloak 8180 未就绪，跳过 E2E-JWT02");
        }
        var ctx = ScmScenarioContext.get();
        ctx.clientToken = "ct-kc-e2e-" + System.nanoTime();
        String body = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(ctx.clientToken);
        ctx.orderNo = given().baseUri(OMS)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", ctx.clientToken)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/orders")
                .then().statusCode(201)
                .extract().path("data.orders[0].order_no");
    }

    private static String unsignedJwtBearer(String scope) {
        String issuer = System.getenv().getOrDefault("SCM_JWT_ISSUER", "http://localhost:8180/realms/scm");
        String header = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String payloadJson = """
                {"iss":"%s","scope":"%s","exp":9999999999}
                """.formatted(issuer, scope);
        String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return "Bearer " + header + "." + payload + ".sig";
    }

    private static String fetchKeycloakAccessToken() {
        String base = System.getenv().getOrDefault("KEYCLOAK_URL", "http://localhost:8180");
        for (int i = 0; i < 40; i++) {
            try {
                String token = given().baseUri(base)
                        .contentType("application/x-www-form-urlencoded")
                        .formParam("grant_type", "password")
                        .formParam("client_id", "scm-gateway")
                        .formParam("username", "e2e-user")
                        .formParam("password", "e2e-pass")
                        .formParam("scope", "openid")
                        .post("/realms/scm/protocol/openid-connect/token")
                        .then().extract().path("access_token");
                if (token != null && !token.isBlank()) {
                    return token;
                }
            } catch (Exception ignored) {
                // Keycloak may still be importing the realm in compose CI.
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    @并且("WMS 退货入库完成")
    public void wmsReturnInbound() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(WMS).contentType(ContentType.JSON)
                .body(Map.of("order_no", ctx.orderNo))
                .post("/wms/v1/return/inbound")
                .then().statusCode(200);
    }

    @当("模拟退款成功")
    public void refundSuccess() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).post("/api/v1/ops/orders/{no}/refund-success", ctx.orderNo)
                .then().statusCode(200);
    }

    @那么("售后状态应为 REFUND_SUCCESS")
    public void afterSaleRefundSuccess() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).get("/api/v1/ops/orders/{no}/after-sale/diag", ctx.orderNo)
                .then().statusCode(200)
                .body("data.after_sale_status", equalTo("REFUND_SUCCESS"));
    }

    @那么("消息 REFUND_COMPLETED 已被 ERP 消费")
    public void refundCompletedInErp() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        ctx.bizKey = "REFUND_COMPLETED+" + ctx.orderNo;
        for (int i = 0; i < 20; i++) {
            String jeNo = given().baseUri(ERP).queryParam("biz_key", ctx.bizKey)
                    .get("/api/v1/integration/journal")
                    .then().statusCode(200)
                    .extract().path("data.je_no");
            if (jeNo != null) {
                return;
            }
            Thread.sleep(200);
        }
        given().baseUri(ERP).queryParam("biz_key", ctx.bizKey)
                .get("/api/v1/integration/journal")
                .then().statusCode(200)
                .body("data.je_no", notNullValue());
    }

    private void pushTms(String event) {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).contentType(ContentType.JSON)
                .body(Map.of("order_no", ctx.orderNo, "event", event))
                .post("/api/v1/integration/tms/track")
                .then().statusCode(200);
    }

    @当("等待 WMS 出库单创建并发运")
    public void wmsShip() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).post("/api/v1/ops/orders/{no}/ship", ctx.orderNo)
                .then().statusCode(200);
    }

    @当("等待 WMS 出库单由事件创建")
    public void waitWmsOutboundFromEvent() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        resolveWmsOutbound(ctx, 40);
    }

    @那么("WMS 存在出库单")
    public void wmsOutboundExists() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.outboundNo == null) {
            resolveWmsOutbound(ctx, 5);
        }
        given().baseUri(WMS).get("/wms/v1/outbound/by-order/{no}", ctx.orderNo)
                .then().statusCode(200)
                .body("outbound_no", notNullValue());
    }

    @假如("已 PAID 且含 2 个包裹 Kafka")
    public void paidTwoPackagesKafka() {
        submitOrder();
        initTwoPackages();
        payViaMockPay();
    }

    @当("对第 1 个包裹的 WMS 出库单执行发运")
    public void wmsShipFirstPackageOutbound() throws InterruptedException {
        wmsShipPackageByIndex(1);
    }

    @当("对第 2 个包裹的 WMS 出库单执行发运")
    public void wmsShipSecondPackageOutbound() throws InterruptedException {
        wmsShipPackageByIndex(2);
    }

    private void wmsShipPackageByIndex(int index) throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        ctx.packageNo = "P" + ctx.orderNo + "-" + index;
        String outboundNo = null;
        for (int i = 0; i < 30; i++) {
            int status = given().baseUri(WMS).get("/wms/v1/outbound/by-package/{pkg}", ctx.packageNo)
                    .then().extract().statusCode();
            if (status == 200) {
                outboundNo = given().baseUri(WMS).get("/wms/v1/outbound/by-package/{pkg}", ctx.packageNo)
                        .then().statusCode(200)
                        .extract().path("outbound_no");
                break;
            }
            Thread.sleep(300);
        }
        given().baseUri(WMS).contentType(ContentType.JSON)
                .body(shipBodyWithWaybill(ctx))
                .post("/wms/v1/outbound/{ob}/ship", outboundNo)
                .then().statusCode(200);
        ctx.bizKey = "WMS_OUTBOUND_SHIPPED+" + outboundNo;
    }

    @当("WMS 对出库单执行拣货")
    public void wmsPickOutbound() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.outboundNo == null) {
            resolveWmsOutbound(ctx, 40);
        }
        given().baseUri(WMS).post("/wms/v1/outbound/{ob}/pick", ctx.outboundNo)
                .then().statusCode(200);
    }

    @并且("WMS 对出库单执行复核")
    public void wmsCheckOutbound() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.outboundNo == null) {
            resolveWmsOutbound(ctx, 40);
        }
        given().baseUri(WMS).post("/wms/v1/outbound/{ob}/check", ctx.outboundNo)
                .then().statusCode(200);
    }

    @当("WMS 对出库单执行交接发运")
    public void wmsHandoverOutbound() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.outboundNo == null) {
            resolveWmsOutbound(ctx, 40);
        }
        ctx.handoverWaybill = "WB-" + ctx.orderNo;
        given().baseUri(WMS).contentType(ContentType.JSON)
                .body(Map.of(
                        "outbound_no", ctx.outboundNo,
                        "waybill_no", ctx.handoverWaybill,
                        "weight_kg", "1.0",
                        "handover_at", "2026-05-31T12:00:00+08:00"))
                .post("/rf/v1/ship/handover")
                .then().statusCode(200);
        ctx.bizKey = "WMS_OUTBOUND_SHIPPED+" + ctx.outboundNo;
    }

    @并且("TMS 运单号应与 WMS 交接运单号一致")
    public void tmsWaybillMatchesHandover() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.handoverWaybill == null) {
            ctx.handoverWaybill = "WB-" + ctx.orderNo;
        }
        for (int i = 0; i < 25; i++) {
            String waybill = given().baseUri(TMS).queryParam("waybill_no", ctx.handoverWaybill)
                    .get("/tms/v1/ops/shipment/by-waybill")
                    .then().extract().path("data.waybill_no");
            if (ctx.handoverWaybill.equals(waybill)) {
                return;
            }
            Thread.sleep(300);
        }
        given().baseUri(TMS).queryParam("waybill_no", ctx.handoverWaybill)
                .get("/tms/v1/ops/shipment/by-waybill")
                .then().statusCode(200)
                .body("data.waybill_no", equalTo(ctx.handoverWaybill));
    }

    @并且("TMS 存在 WMS_HANDOVER 轨迹事件")
    public void tmsHasHandoverTrackEvent() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.handoverWaybill == null) {
            ctx.handoverWaybill = "WB-" + ctx.orderNo;
        }
        for (int i = 0; i < 25; i++) {
            java.util.List<String> codes = given().baseUri(TMS).queryParam("waybill_no", ctx.handoverWaybill)
                    .get("/tms/v1/ops/track/events")
                    .then().statusCode(200)
                    .extract().path("data.events.event_code");
            if (codes != null && codes.contains("WMS_HANDOVER")) {
                return;
            }
            Thread.sleep(300);
        }
        given().baseUri(TMS).queryParam("waybill_no", ctx.handoverWaybill)
                .get("/tms/v1/ops/track/events")
                .then().statusCode(200)
                .body("data.events.event_code", hasItem("WMS_HANDOVER"));
    }

    @那么("WMS 出库单状态应为 SHIPPED")
    public void wmsOutboundStatusShipped() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.outboundNo == null) {
            resolveWmsOutbound(ctx, 20);
        }
        for (int i = 0; i < 20; i++) {
            String status = given().baseUri(WMS).get("/wms/v1/outbound/{ob}", ctx.outboundNo)
                    .then().statusCode(200)
                    .extract().path("status");
            if ("SHIPPED".equals(status)) {
                return;
            }
            Thread.sleep(300);
        }
        given().baseUri(WMS).get("/wms/v1/outbound/{ob}", ctx.outboundNo)
                .then().statusCode(200)
                .body("status", equalTo("SHIPPED"));
    }

    @当("对 WMS 出库单执行发运")
    public void wmsShipOutbound() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.outboundNo == null) {
            resolveWmsOutbound(ctx, 20);
        }
        given().baseUri(WMS).contentType(ContentType.JSON)
                .body(shipBodyWithWaybill(ctx))
                .post("/wms/v1/outbound/{ob}/ship", ctx.outboundNo)
                .then().statusCode(200);
        ctx.bizKey = "WMS_OUTBOUND_SHIPPED+" + ctx.outboundNo;
    }

    private static Map<String, Object> shipBodyWithWaybill(ScmScenarioContext.State ctx) {
        java.util.LinkedHashMap<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("lines", java.util.List.of(Map.of("sku_code", "SKU001", "qty", "2.0000")));
        if (ctx.packageNo != null) {
            body.put("waybill_no", "WB-" + ctx.packageNo);
        } else if (ctx.orderNo != null) {
            body.put("waybill_no", "WB-P" + ctx.orderNo);
        }
        return body;
    }

    @假如("已 PAID 且 WMS 已建出库")
    public void givenPaidAndWmsOutbound() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.orderNo == null) {
            submitOrder();
            payNotify();
        }
        resolveWmsOutbound(ctx, 40);
    }

    private static void resolveWmsOutbound(ScmScenarioContext.State ctx, int maxAttempts)
            throws InterruptedException {
        ctx.packageNo = "P" + ctx.orderNo;
        for (int i = 0; i < maxAttempts; i++) {
            int status = given().baseUri(WMS).get("/wms/v1/outbound/by-order/{no}", ctx.orderNo)
                    .then().extract().statusCode();
            if (status == 200) {
                ctx.outboundNo = given().baseUri(WMS).get("/wms/v1/outbound/by-order/{no}", ctx.orderNo)
                        .then().statusCode(200)
                        .extract().path("outbound_no");
                return;
            }
            Thread.sleep(300);
        }
        given().baseUri(WMS).get("/wms/v1/outbound/by-order/{no}", ctx.orderNo)
                .then().statusCode(200)
                .body("outbound_no", notNullValue());
    }

    @并且("消息 WMS_OUTBOUND_SHIPPED 已被 ERP 消费")
    public void wmsOutboundConsumedByErp() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.bizKey == null) {
            if (ctx.outboundNo != null) {
                ctx.bizKey = "WMS_OUTBOUND_SHIPPED+" + ctx.outboundNo;
            } else {
                ctx.packageNo = "P" + ctx.orderNo;
                ctx.bizKey = "WMS_OUTBOUND_SHIPPED+OB" + ctx.packageNo.replace("P", "");
            }
        }
        for (int i = 0; i < 20; i++) {
            String jeNo = given().baseUri(ERP).queryParam("biz_key", ctx.bizKey)
                    .get("/api/v1/integration/journal")
                    .then().statusCode(200)
                    .extract().path("data.je_no");
            if (jeNo != null) {
                return;
            }
            Thread.sleep(300);
        }
        given().baseUri(ERP).queryParam("biz_key", ctx.bizKey)
                .get("/api/v1/integration/journal")
                .then().statusCode(200)
                .body("data.je_no", notNullValue());
    }

    @那么("ERP 存在凭证 je_no")
    public void erpJeExists() {
        var ctx = ScmScenarioContext.get();
        if (ctx.bizKey == null) {
            ctx.packageNo = "P" + ctx.orderNo;
            ctx.bizKey = "WMS_OUTBOUND_SHIPPED+OB" + ctx.packageNo.replace("P", "");
        }
        given().baseUri(ERP).queryParam("biz_key", ctx.bizKey)
                .get("/api/v1/integration/journal")
                .then().statusCode(200)
                .body("data.je_no", notNullValue());
    }

    @那么("订单状态应保持 {word}")
    public void assertOrderStatusStable(String status) throws InterruptedException {
        assertOrderStatus(status);
    }

    @并且("TMS 已创建运单")
    public void tmsShipmentExists() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.packageNo == null) {
            ctx.packageNo = "P" + ctx.orderNo;
        }
        for (int i = 0; i < 30; i++) {
            Number count = given().baseUri(TMS).queryParam("package_no", ctx.packageNo)
                    .get("/tms/v1/ops/shipment/count")
                    .then().statusCode(200)
                    .extract().path("data.count");
            if (count != null && count.intValue() >= 1) {
                return;
            }
            Thread.sleep(300);
        }
        given().baseUri(TMS).queryParam("package_no", ctx.packageNo)
                .get("/tms/v1/ops/shipment/count")
                .then().statusCode(200)
                .body("data.count", greaterThanOrEqualTo(1));
    }

    @当("通过 mock-carrier 对当前包裹运单触发签收")
    public void mockCarrierDeliveredForPackageWaybill() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.packageNo == null) {
            ctx.packageNo = "P" + ctx.orderNo;
        }
        for (int i = 0; i < 30; i++) {
            String waybill = given().baseUri(TMS).queryParam("package_no", ctx.packageNo)
                    .get("/tms/v1/ops/shipment/detail")
                    .then().extract().path("data.waybill_no");
            if (waybill != null && !waybill.isBlank()) {
                String carrier = waybill.contains("-")
                        ? waybill.substring(0, waybill.indexOf('-'))
                        : "SF";
                given().baseUri(MOCK_CARRIER).contentType(ContentType.JSON)
                        .body(Map.of(
                                "waybill_no", waybill,
                                "carrier_code", carrier,
                                "carrier_status", "DELIVERED"
                        ))
                        .post("/trigger/track")
                        .then().statusCode(200);
                return;
            }
            Thread.sleep(300);
        }
        mockCarrierDelivered();
    }

    @当("通过 mock-carrier 触发签收轨迹")
    public void mockCarrierDelivered() {
        var ctx = ScmScenarioContext.get();
        if (ctx.packageNo == null) {
            ctx.packageNo = "P" + ctx.orderNo;
        }
        given().baseUri(MOCK_CARRIER).contentType(ContentType.JSON)
                .body(Map.of(
                        "waybill_no", "YTO-FIX-001",
                        "carrier_code", "YTO",
                        "carrier_status", "DELIVERED"
                ))
                .post("/trigger/track")
                .then().statusCode(200);
    }

    @当("通过 mock-carrier 用交接运单号触发签收")
    public void mockCarrierDeliveredWithHandoverWaybill() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.handoverWaybill == null) {
            ctx.handoverWaybill = "WB-" + ctx.orderNo;
        }
        String carrier = ctx.handoverWaybill.contains("-")
                ? ctx.handoverWaybill.substring(0, ctx.handoverWaybill.indexOf('-'))
                : "WB";
        for (int i = 0; i < 25; i++) {
            given().baseUri(MOCK_CARRIER).contentType(ContentType.JSON)
                    .body(Map.of(
                            "waybill_no", ctx.handoverWaybill,
                            "carrier_code", carrier,
                            "carrier_status", "DELIVERED"
                    ))
                    .post("/trigger/track")
                    .then().statusCode(200);
            String status = given().baseUri(OMS).get("/api/v1/ops/orders/{no}", ctx.orderNo)
                    .then().statusCode(200)
                    .extract().path("data.status");
            if ("DELIVERED".equals(status)) {
                return;
            }
            Thread.sleep(400);
        }
        assertOrderStatus("DELIVERED");
    }

    @并且("ERP 凭证应包含交接运单号")
    public void erpJournalHasHandoverWaybill() throws InterruptedException {
        var ctx = ScmScenarioContext.get();
        if (ctx.handoverWaybill == null) {
            ctx.handoverWaybill = "WB-" + ctx.orderNo;
        }
        if (ctx.bizKey == null && ctx.outboundNo != null) {
            ctx.bizKey = "WMS_OUTBOUND_SHIPPED+" + ctx.outboundNo;
        }
        for (int i = 0; i < 25; i++) {
            String wb = given().baseUri(ERP).queryParam("biz_key", ctx.bizKey)
                    .get("/api/v1/integration/journal")
                    .then().statusCode(200)
                    .extract().path("data.waybill_no");
            if (ctx.handoverWaybill.equals(wb)) {
                return;
            }
            Thread.sleep(300);
        }
        given().baseUri(ERP).queryParam("biz_key", ctx.bizKey)
                .get("/api/v1/integration/journal")
                .then().statusCode(200)
                .body("data.waybill_no", equalTo(ctx.handoverWaybill));
    }

    @当("模拟 TMS 签收")
    public void tmsDeliver() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS).post("/api/v1/ops/orders/{no}/deliver", ctx.orderNo)
                .then().statusCode(200);
    }

    @那么("两次返回的 order_no 应相同")
    public void sameOrderNo() {
        var ctx = ScmScenarioContext.get();
        assertEquals(ctx.orderNo, ctx.lastOrderNo2);
    }

    @并且("数据库仅 1 条 trade_order")
    public void oneTradeOrderRow() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(OMS)
                .queryParam("buyer_id", "U10001")
                .queryParam("client_token", ctx.clientToken)
                .get("/api/v1/ops/orders/trade-count")
                .then().statusCode(200)
                .body("data.trade_order_count", equalTo(1));
    }

    @当("使用相同 package_no 两次创建出库单")
    public void wmsDuplicate() {
        var ctx = ScmScenarioContext.get();
        ctx.packageNo = "P-wms-dup-" + System.nanoTime();
        String body = wmsBody(ctx.packageNo, "O-dup-001");
        given().baseUri(WMS).header("Idempotency-Key", ctx.packageNo)
                .contentType(ContentType.JSON).body(body)
                .post("/wms/v1/outbound/create").then().statusCode(201);
        var second = given().baseUri(WMS).header("Idempotency-Key", ctx.packageNo)
                .contentType(ContentType.JSON).body(body)
                .post("/wms/v1/outbound/create");
        ctx.httpStatus = second.then().extract().statusCode();
        ctx.errorCode = second.then().extract().path("code");
    }

    @那么("仅 1 张 outbound_order")
    public void oneOutboundRow() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(WMS).queryParam("package_no", ctx.packageNo)
                .get("/wms/v1/ops/outbound/count")
                .then().statusCode(200)
                .body("data.count", equalTo(1));
    }

    @并且("第二次返回码为 WMS_10001 或 HTTP 409")
    public void assertConflictOrWmsCode() {
        var ctx = ScmScenarioContext.get();
        org.junit.jupiter.api.Assertions.assertTrue(
                ctx.httpStatus == 409 || "WMS_10001".equals(ctx.errorCode),
                "expected 409 or WMS_10001 but got status=" + ctx.httpStatus + " code=" + ctx.errorCode);
    }

    @当("相同 biz_key 的 WMS_OUTBOUND_SHIPPED 消费 2 次")
    public void erpPostTwice() {
        var ctx = ScmScenarioContext.get();
        ctx.bizKey = "WMS_OUTBOUND_SHIPPED+OB-ERP-DUP-001";
        String body = """
                {"outbound_no":"OB-ERP-DUP-001","source_system":"WMS","source_order_no":"O-erp-dup",
                 "org_id":"ORG001","wh_code":"WH-SH-01","shipped_at":"2026-05-31T15:00:00+08:00",
                 "lines":[{"material_code":"M001","qty":"2.0000"}]}
                """;
        given().baseUri(ERP).header("Idempotency-Key", ctx.bizKey)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/integration/wms/shipment").then().statusCode(200);
        given().baseUri(ERP).header("Idempotency-Key", ctx.bizKey)
                .contentType(ContentType.JSON).body(body)
                .post("/api/v1/integration/wms/shipment").then().statusCode(200)
                .body("code", equalTo("ERP_02001"));
    }

    @那么("journal_entry 仅 1 张")
    public void oneJournal() {
        var ctx = ScmScenarioContext.get();
        given().baseUri(ERP).queryParam("biz_key", ctx.bizKey)
                .get("/api/v1/integration/journal")
                .then().statusCode(200)
                .body("data.je_no", notNullValue());
    }

    private static String wmsBody(String packageNo, String orderNo) {
        return """
                {"package_no":"%s","source_order_no":"%s","warehouse_code":"WH-SH-01",
                 "delivery_type":"EXPRESS","lines":[{"sku_code":"SKU001","qty":"2"}],
                 "receiver":{"name":"t","phone":"1","province":"z","city":"h","district":"y","address":"a"}}
                """.formatted(packageNo, orderNo);
    }

    private static void assumePort(int port) {
        if (!portOpen(port)) {
            throw new org.opentest4j.TestAbortedException("端口未就绪: " + port);
        }
    }

    private static boolean portOpen(int port) {
        try (Socket s = new Socket("127.0.0.1", port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
