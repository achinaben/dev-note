package com.scm.oms.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class WmsFulfillmentClient implements WmsGateway {
    @Value("${wms.base-url:http://localhost:8082}")
    private String wmsBaseUrl;

    private final RestTemplate rest = new RestTemplate();

    @Override
    @SuppressWarnings("unchecked")
    public String createOutbound(String packageNo, String orderNo) {
        Optional<String> existing = findOutboundByPackage(packageNo);
        if (existing.isPresent()) {
            return existing.get();
        }
        Map<String, Object> body = buildCreateBody(packageNo, orderNo);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", packageNo);
        try {
            var resp = rest.postForEntity(
                    wmsBaseUrl + "/wms/v1/outbound/create",
                    new HttpEntity<>(body, headers),
                    Map.class);
            return (String) resp.getBody().get("outbound_no");
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                Map<String, Object> conflict = ex.getResponseBodyAs(Map.class);
                if (conflict != null && conflict.get("outbound_no") != null) {
                    return (String) conflict.get("outbound_no");
                }
            }
            throw ex;
        }
    }

    private Optional<String> findOutboundByPackage(String packageNo) {
        try {
            var resp = rest.getForEntity(
                    wmsBaseUrl + "/wms/v1/outbound/by-package/{packageNo}",
                    Map.class,
                    packageNo);
            if (resp.getBody() != null && resp.getBody().get("outbound_no") != null) {
                return Optional.of((String) resp.getBody().get("outbound_no"));
            }
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> findOutboundByOrder(String orderNo) {
        try {
            var resp = rest.getForEntity(
                    wmsBaseUrl + "/wms/v1/outbound/by-order/{orderNo}",
                    Map.class,
                    orderNo);
            if (resp.getBody() != null && resp.getBody().get("outbound_no") != null) {
                return Optional.of((String) resp.getBody().get("outbound_no"));
            }
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
        return Optional.empty();
    }

    @Override
    public void ship(String outboundNo) {
        ship(outboundNo, null);
    }

    @Override
    public void ship(String outboundNo, String waybillNo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("lines", List.of(Map.of("sku_code", "SKU001", "qty", "2.0000")));
        if (waybillNo != null && !waybillNo.isBlank()) {
            body.put("waybill_no", waybillNo);
        }
        rest.postForEntity(
                wmsBaseUrl + "/wms/v1/outbound/{ob}/ship",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class,
                outboundNo);
    }

    private static Map<String, Object> buildCreateBody(String packageNo, String orderNo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("package_no", packageNo);
        body.put("source_order_no", orderNo);
        body.put("warehouse_code", "WH-SH-01");
        body.put("delivery_type", "EXPRESS");
        body.put("lines", List.of(Map.of("sku_code", "SKU001", "qty", "2")));
        body.put("receiver", Map.of(
                "name", "测试", "phone", "13800000000",
                "province", "浙江省", "city", "杭州市", "district", "余杭区", "address", "文一西路"
        ));
        return body;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
