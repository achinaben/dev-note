package com.scm.tms.shipment;

import com.scm.tms.track.TrackEventRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ShipmentApplicationService {
    private final ShipmentStore shipmentStore;
    private final TrackEventRepository trackEventRepository;

    public ShipmentApplicationService(ShipmentStore shipmentStore, TrackEventRepository trackEventRepository) {
        this.shipmentStore = shipmentStore;
        this.trackEventRepository = trackEventRepository;
    }

    public CreateResult create(Map<String, Object> body) {
        String packageNo = (String) body.get("package_no");
        String incomingWaybill = waybillFromBody(body);
        Optional<ShipmentRecord> existing = shipmentStore.findByPackageNo(packageNo);
        if (existing.isPresent()) {
            ShipmentRecord ex = existing.get();
            mergeWaybillOnReplay(packageNo, ex, incomingWaybill);
            return CreateResult.conflict(toResponse(ex));
        }
        ShipmentRecord r = new ShipmentRecord();
        r.setPackageNo(packageNo);
        r.setOrderNo((String) body.get("order_no"));
        String carrier = body.containsKey("carrier_code")
                ? String.valueOf(body.get("carrier_code"))
                : "SF";
        r.setShipmentNo("SH" + packageNo);
        r.setCarrierCode(carrier);
        r.setWaybillNo(resolveWaybillForCreate(carrier, incomingWaybill));
        r.setLabelUrl("http://localhost/label");
        r.setStatus("CREATED");
        try {
            shipmentStore.insert(r);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return shipmentStore.findByPackageNo(packageNo)
                    .map(s -> CreateResult.conflict(toResponse(s)))
                    .orElseThrow(() -> new IllegalStateException("duplicate shipment", e));
        }
        return CreateResult.created(toResponse(r));
    }

    public void markDelivered(String packageNo) {
        shipmentStore.updateStatusByPackageNo(packageNo, "DELIVERED");
    }

    public long countByPackageNo(String packageNo) {
        return shipmentStore.countByPackageNo(packageNo);
    }

    public Optional<ShipmentRecord> findByWaybillNo(String waybillNo) {
        return shipmentStore.findByWaybillNo(waybillNo);
    }

    public Optional<ShipmentRecord> findByPackageNo(String packageNo) {
        return shipmentStore.findByPackageNo(packageNo);
    }

    public Map<String, Object> intercept(String shipmentNo) {
        ShipmentRecord r = shipmentStore.findByShipmentNo(shipmentNo)
                .orElseThrow(() -> new IllegalArgumentException("TMS_10011"));
        shipmentStore.updateStatusByShipmentNo(shipmentNo, "INTERCEPTED");
        return Map.of(
                "shipment_no", shipmentNo,
                "status", "INTERCEPTED",
                "package_no", r.getPackageNo()
        );
    }

    /**
     * WMS 交接发运时绑定运单号：已存在则更新 waybill 并记轨迹；不存在则按包裹建单。
     */
    public Map<String, Object> bindWaybillFromHandover(String packageNo, String orderNo, String waybillNo) {
        if (waybillNo == null || waybillNo.isBlank()) {
            throw new IllegalArgumentException("waybill_no 不能为空");
        }
        Optional<ShipmentRecord> existing = shipmentStore.findByPackageNo(packageNo);
        ShipmentRecord r;
        if (existing.isPresent()) {
            r = existing.get();
            shipmentStore.updateWaybillByPackageNo(packageNo, waybillNo);
            r.setWaybillNo(waybillNo);
        } else {
            r = new ShipmentRecord();
            r.setPackageNo(packageNo);
            r.setOrderNo(orderNo);
            r.setShipmentNo("SH" + packageNo);
            String carrier = inferCarrier(waybillNo);
            r.setCarrierCode(carrier);
            r.setWaybillNo(waybillNo);
            r.setLabelUrl("http://localhost/label");
            r.setStatus("CREATED");
            shipmentStore.insert(r);
        }
        trackEventRepository.append(waybillNo, "WMS_HANDOVER", "wms");
        return toResponse(r);
    }

    public List<Map<String, Object>> listTrackEvents(String waybillNo) {
        return trackEventRepository.listByWaybill(waybillNo).stream()
                .map(e -> Map.<String, Object>of(
                        "waybill_no", e.getWaybillNo(),
                        "event_code", e.getEventCode(),
                        "source", e.getSource(),
                        "event_at", e.getEventAt().toString()))
                .toList();
    }

    static boolean isPlaceholderWaybill(String waybillNo) {
        return waybillNo == null || waybillNo.isBlank() || waybillNo.endsWith("-FIX-001");
    }

    private static String waybillFromBody(Map<String, Object> body) {
        if (!body.containsKey("waybill_no") || body.get("waybill_no") == null) {
            return null;
        }
        String wb = String.valueOf(body.get("waybill_no")).trim();
        return wb.isEmpty() ? null : wb;
    }

    private static String resolveWaybillForCreate(String carrier, String incomingWaybill) {
        if (incomingWaybill != null && !incomingWaybill.isBlank()) {
            return incomingWaybill;
        }
        return carrier + "-FIX-001";
    }

    private void mergeWaybillOnReplay(String packageNo, ShipmentRecord ex, String incomingWaybill) {
        if (incomingWaybill == null || incomingWaybill.isBlank() || isPlaceholderWaybill(incomingWaybill)) {
            return;
        }
        if (incomingWaybill.equals(ex.getWaybillNo())) {
            return;
        }
        if (isPlaceholderWaybill(ex.getWaybillNo())) {
            shipmentStore.updateWaybillByPackageNo(packageNo, incomingWaybill);
            ex.setWaybillNo(incomingWaybill);
        }
    }

    private static String inferCarrier(String waybillNo) {
        int dash = waybillNo.indexOf('-');
        if (dash > 0) {
            return waybillNo.substring(0, dash);
        }
        return "SF";
    }

    public String mapCarrierStatusToOmsEvent(String carrierStatus) {
        return switch (carrierStatus) {
            case "IN_TRANSIT", "PICKED_UP" -> "TMS_IN_TRANSIT";
            case "DELIVERED", "SIGNED" -> "TMS_DELIVERED";
            default -> null;
        };
    }

    private static Map<String, Object> toResponse(ShipmentRecord r) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("shipment_no", r.getShipmentNo());
        data.put("waybill_no", r.getWaybillNo());
        data.put("label_url", r.getLabelUrl());
        data.put("carrier_code", r.getCarrierCode());
        data.put("status", r.getStatus());
        return data;
    }

    public record CreateResult(boolean conflict, Map<String, Object> data) {
        static CreateResult created(Map<String, Object> data) {
            return new CreateResult(false, data);
        }

        static CreateResult conflict(Map<String, Object> data) {
            return new CreateResult(true, data);
        }
    }
}
