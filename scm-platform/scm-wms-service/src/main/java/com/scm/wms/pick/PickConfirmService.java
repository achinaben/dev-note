package com.scm.wms.pick;

import com.scm.wms.outbound.OutboundShipmentService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PickConfirmService {

    private final PickConfirmStore pickConfirmStore;
    private final OutboundShipmentService shipmentService;

    public PickConfirmService(PickConfirmStore pickConfirmStore, OutboundShipmentService shipmentService) {
        this.pickConfirmStore = pickConfirmStore;
        this.shipmentService = shipmentService;
    }

    public Map<String, Object> confirm(Map<String, Object> body) {
        String operationId = String.valueOf(body.get("operation_id"));
        if (pickConfirmStore.exists(operationId)) {
            String outboundNo = pickConfirmStore.findOutboundNo(operationId).orElseThrow();
            return Map.of(
                    "operation_id", operationId,
                    "outbound_no", outboundNo,
                    "status", "PICKED",
                    "idempotent", true);
        }
        String outboundNo = resolveOutboundNo(body);
        shipmentService.confirmPick(outboundNo);
        pickConfirmStore.save(operationId, outboundNo);
        return Map.of(
                "operation_id", operationId,
                "outbound_no", outboundNo,
                "status", "PICKED",
                "idempotent", false);
    }

    private static String resolveOutboundNo(Map<String, Object> body) {
        if (body.containsKey("outbound_no")) {
            return String.valueOf(body.get("outbound_no"));
        }
        String taskNo = String.valueOf(body.get("task_no"));
        if (taskNo.startsWith("OB")) {
            return taskNo;
        }
        if (taskNo.startsWith("TASK-")) {
            return taskNo.substring("TASK-".length());
        }
        throw new IllegalArgumentException("无法解析 outbound_no，请传 outbound_no 或 task_no=OB...");
    }
}
