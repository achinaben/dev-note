package com.scm.wms.outbound;

import com.scm.wms.integration.ErpShipmentClient;
import com.scm.wms.integration.TmsHandoverClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OutboundShipmentService {

    private final OutboundStore outboundStore;
    private final OutboundStateMachine stateMachine;
    private final ErpShipmentClient erpShipmentClient;
    private final TmsHandoverClient tmsHandoverClient;
    private final OutboundWmsProperties properties;

    public OutboundShipmentService(
            OutboundStore outboundStore,
            OutboundStateMachine stateMachine,
            ErpShipmentClient erpShipmentClient,
            TmsHandoverClient tmsHandoverClient,
            OutboundWmsProperties properties) {
        this.outboundStore = outboundStore;
        this.stateMachine = stateMachine;
        this.erpShipmentClient = erpShipmentClient;
        this.tmsHandoverClient = tmsHandoverClient;
        this.properties = properties;
    }

    public Optional<OutboundRecord> find(String outboundNo) {
        return outboundStore.findByOutboundNo(outboundNo);
    }

    public void confirmPick(String outboundNo) {
        OutboundRecord ob = require(outboundNo);
        OutboundStatus next = stateMachine.apply(OutboundStatus.parse(ob.getStatus()), OutboundStatus.PICKED);
        outboundStore.updateStatus(outboundNo, next.name());
    }

    public void confirmCheck(String outboundNo) {
        OutboundRecord ob = require(outboundNo);
        OutboundStatus current = OutboundStatus.parse(ob.getStatus());
        OutboundStatus next = current == OutboundStatus.CREATED
                ? stateMachine.fastForwardToChecked(current)
                : stateMachine.apply(current, OutboundStatus.CHECKED);
        outboundStore.updateStatus(outboundNo, next.name());
    }

    public Map<String, Object> handover(String outboundNo, List<Map<String, String>> lines, Map<String, Object> handoverMeta) {
        OutboundRecord ob = require(outboundNo);
        OutboundStatus current = OutboundStatus.parse(ob.getStatus());
        if (current == OutboundStatus.CREATED && properties.isRelaxedHandover()) {
            current = stateMachine.fastForwardToChecked(current);
            outboundStore.updateStatus(outboundNo, current.name());
        }
        if (current != OutboundStatus.CHECKED) {
            throw new IllegalStateException("出库单须为 CHECKED 才能交接发运，当前=" + current);
        }
        stateMachine.apply(current, OutboundStatus.SHIPPED);
        tmsHandoverClient.bindWaybillIfPresent(ob.getPackageNo(), ob.getSourceOrderNo(), handoverMeta);
        erpShipmentClient.notifyOutboundShipped(
                outboundNo, ob.getSourceOrderNo(), ob.getPackageNo(), lines, handoverMeta);
        outboundStore.updateStatus(outboundNo, OutboundStatus.SHIPPED.name());
        return Map.of("outbound_no", outboundNo, "status", "SHIPPED");
    }

    private OutboundRecord require(String outboundNo) {
        return outboundStore.findByOutboundNo(outboundNo)
                .orElseThrow(() -> new IllegalArgumentException("出库单不存在: " + outboundNo));
    }
}
