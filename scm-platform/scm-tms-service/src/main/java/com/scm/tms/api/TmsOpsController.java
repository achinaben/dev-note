package com.scm.tms.api;

import com.scm.common.web.ApiResponse;
import com.scm.tms.shipment.ShipmentApplicationService;
import com.scm.tms.shipment.ShipmentRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tms/v1/ops")
public class TmsOpsController {
    private final ShipmentApplicationService shipmentService;

    public TmsOpsController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/shipment/count")
    public ApiResponse<Map<String, Object>> shipmentCount(@RequestParam("package_no") String packageNo) {
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "package_no", packageNo,
                "count", shipmentService.countByPackageNo(packageNo)
        ));
    }

    @GetMapping("/shipment/detail")
    public ApiResponse<Map<String, Object>> shipmentByPackage(@RequestParam("package_no") String packageNo) {
        var r = shipmentService.findByPackageNo(packageNo)
                .orElseThrow(() -> new IllegalArgumentException("TMS_10010"));
        return ApiResponse.ok(UUID.randomUUID().toString(), shipmentDetail(r));
    }

    @GetMapping("/shipment/by-waybill")
    public ApiResponse<Map<String, Object>> shipmentByWaybill(@RequestParam("waybill_no") String waybillNo) {
        var r = shipmentService.findByWaybillNo(waybillNo)
                .orElseThrow(() -> new IllegalArgumentException("TMS_10010"));
        return ApiResponse.ok(UUID.randomUUID().toString(), shipmentDetail(r));
    }

    @GetMapping("/track/events")
    public ApiResponse<Map<String, Object>> trackEvents(@RequestParam("waybill_no") String waybillNo) {
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "waybill_no", waybillNo,
                "events", shipmentService.listTrackEvents(waybillNo)
        ));
    }

    private static Map<String, Object> shipmentDetail(ShipmentRecord r) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("package_no", r.getPackageNo());
        data.put("order_no", r.getOrderNo() == null ? "" : r.getOrderNo());
        data.put("shipment_no", r.getShipmentNo());
        data.put("waybill_no", r.getWaybillNo());
        data.put("status", r.getStatus());
        return data;
    }
}
