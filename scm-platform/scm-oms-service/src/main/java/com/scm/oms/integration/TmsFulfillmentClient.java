package com.scm.oms.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class TmsFulfillmentClient {
    @Value("${tms.base-url:http://localhost:8083}")
    private String tmsBaseUrl;

    @Value("${scm.fulfillment.use-recommended-carrier:true}")
    private boolean useRecommendedCarrier;

    private final RestTemplate rest = new RestTemplate();
    private final TmsFreightClient freightClient;

    public TmsFulfillmentClient(TmsFreightClient freightClient) {
        this.freightClient = freightClient;
    }

    /** 发运后自动下物流单（幂等键 = package_no），默认按运费择优选承运商。 */
    public void createShipment(String packageNo, String orderNo) {
        String carrier = useRecommendedCarrier ? freightClient.recommendedCarrierCode() : "SF";
        createShipment(packageNo, orderNo, carrier);
    }

    public void createShipment(String packageNo, String orderNo, String carrierCode) {
        createShipment(packageNo, orderNo, carrierCode, null);
    }

    /** 携带 WMS 事件中的运单号，TMS 幂等重放时不覆盖已绑定运单。 */
    public void createShipmentWithWaybill(String packageNo, String orderNo, String waybillNo) {
        String carrier = useRecommendedCarrier ? freightClient.recommendedCarrierCode() : "SF";
        createShipment(packageNo, orderNo, carrier, waybillNo);
    }

    public void createShipment(String packageNo, String orderNo, String carrierCode, String waybillNo) {
        HttpHeaders headers = shipmentHeaders(packageNo);
        try {
            rest.postForEntity(tmsBaseUrl + "/tms/v1/shipment/create",
                    new HttpEntity<>(shipmentBody(packageNo, orderNo, carrierCode, waybillNo), headers), Map.class);
        } catch (HttpStatusCodeException e) {
            if (isIdempotentReplay(e)) {
                return;
            }
            throw e;
        }
    }

    public void createAndDeliver(String packageNo, String orderNo) {
        HttpHeaders headers = shipmentHeaders(packageNo);
        createShipment(packageNo, orderNo);
        rest.postForEntity(tmsBaseUrl + "/tms/v1/integration/deliver",
                new HttpEntity<>(Map.of("package_no", packageNo, "order_no", orderNo), headers),
                Map.class);
    }

    private static HttpHeaders shipmentHeaders(String packageNo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", packageNo);
        return headers;
    }

    private static boolean isIdempotentReplay(HttpStatusCodeException e) {
        return e.getStatusCode().value() == 409
                && e.getResponseBodyAsString().contains("\"code\":\"TMS_10001\"");
    }

    private static Map<String, Object> shipmentBody(
            String packageNo, String orderNo, String carrierCode, String waybillNo) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("package_no", packageNo);
        body.put("order_no", orderNo);
        body.put("carrier_code", carrierCode);
        if (waybillNo != null && !waybillNo.isBlank()) {
            body.put("waybill_no", waybillNo);
        }
        body.put("weight_kg", "1.2");
        body.put("volume_cm3", 8000);
        body.put("sender", Map.of("name", "上海仓"));
        body.put("receiver", Map.of("name", "客户", "phone", "13800000000",
                "province", "浙江省", "city", "杭州市", "district", "余杭区", "address", "文一西路"));
        return body;
    }
}
