package com.scm.oms.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class TmsInterceptClient {

    @Value("${tms.base-url:http://localhost:8083}")
    private String tmsBaseUrl;

    @Value("${scm.fulfillment.intercept-on-after-sale:true}")
    private boolean interceptOnAfterSale;

    private final RestTemplate rest = new RestTemplate();

    public void interceptPackages(List<String> packageNos) {
        if (!interceptOnAfterSale || packageNos == null) {
            return;
        }
        for (String packageNo : packageNos) {
            if (packageNo == null || packageNo.isBlank()) {
                continue;
            }
            try {
                String shipmentNo = "SH" + packageNo;
                rest.postForEntity(
                        tmsBaseUrl + "/tms/v1/shipment/{no}/intercept",
                        null,
                        java.util.Map.class,
                        shipmentNo);
            } catch (Exception ignored) {
                // TMS 未就绪时不阻断售后审核
            }
        }
    }
}
