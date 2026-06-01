package com.scm.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.net.Socket;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@EnabledIf("com.scm.e2e.B2CLocalFlowIT#portsReady")
class B2CLocalFlowIT {

    static boolean portsReady() {
        return portOpen(8081) && portOpen(8082) && portOpen(8083) && portOpen(8084)
                && portOpen(8087);
    }

    private static boolean portOpen(int port) {
        try (Socket s = new Socket("127.0.0.1", port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void fullB2CFlowWithJeAssertion() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        String token = "ct-b2c-" + System.nanoTime();
        String orderBody = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(token);

        String orderNo = given().baseUri("http://localhost:8081")
                .header("Idempotency-Key", token)
                .contentType(ContentType.JSON).body(orderBody)
                .post("/api/v1/orders").then().statusCode(201)
                .extract().path("data.orders[0].order_no");

        given().baseUri("http://localhost:8081").contentType(ContentType.JSON)
                .body(Map.of("notify_id", "n-" + token, "order_no", orderNo,
                        "out_trade_no", "PAY", "amount_minor", 19900, "sign_verified", true))
                .post("/api/v1/payments/notify/wechat").then().statusCode(200);

        given().baseUri("http://localhost:8081").get("/api/v1/orders/{n}", orderNo)
                .then().body("data.status", equalTo("PAID"));

        given().baseUri("http://localhost:8081")
                .post("/api/v1/ops/orders/{n}/ship", orderNo).then().statusCode(200);

        given().baseUri("http://localhost:8081").get("/api/v1/orders/{n}", orderNo)
                .then().body("data.status", equalTo("SHIPPED"));

        String packageNo = "P" + orderNo;
        String bizKey = "WMS_OUTBOUND_SHIPPED+OB" + packageNo.replace("P", "");
        given().baseUri("http://localhost:8084").queryParam("biz_key", bizKey)
                .get("/api/v1/integration/journal")
                .then().statusCode(200).body("data.je_no", notNullValue());

        given().baseUri("http://localhost:8081")
                .post("/api/v1/ops/orders/{n}/deliver", orderNo).then().statusCode(200);

        given().baseUri("http://localhost:8081").get("/api/v1/orders/{n}", orderNo)
                .then().body("data.status", equalTo("DELIVERED"));
    }
}
