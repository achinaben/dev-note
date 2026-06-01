package com.scm.wms.outbound;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OutboundApplicationService {
    private final OutboundStore outboundStore;

    public OutboundApplicationService(OutboundStore outboundStore) {
        this.outboundStore = outboundStore;
    }

    public CreateResult create(String idempotencyKey, Map<String, Object> body) {
        String packageNo = (String) body.get("package_no");
        Optional<OutboundRecord> existing = outboundStore.findByPackageNo(packageNo);
        if (existing.isPresent()) {
            return CreateResult.conflict(existing.get().getOutboundNo());
        }
        String ob = "OB" + packageNo.replace("P", "");
        String sourceOrderNo = (String) body.getOrDefault("source_order_no", "O-unknown");
        OutboundRecord record = new OutboundRecord();
        record.setOutboundNo(ob);
        record.setPackageNo(packageNo);
        record.setSourceOrderNo(sourceOrderNo);
        record.setStatus("CREATED");
        Optional<OutboundRecord> raced = outboundStore.findByPackageNo(packageNo);
        if (raced.isPresent()) {
            return CreateResult.conflict(raced.get().getOutboundNo());
        }
        try {
            outboundStore.insert(record);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return outboundStore.findByPackageNo(packageNo)
                    .map(r -> CreateResult.conflict(r.getOutboundNo()))
                    .orElseThrow(() -> new IllegalStateException("duplicate outbound", e));
        }
        return CreateResult.created(ob);
    }

    public Optional<String> findOutboundNoByOrder(String orderNo) {
        return outboundStore.findOutboundNoBySourceOrder(orderNo);
    }

    public Optional<OutboundRecord> findByPackageNo(String packageNo) {
        return outboundStore.findByPackageNo(packageNo);
    }

    public long countByPackageNo(String packageNo) {
        return outboundStore.countByPackageNo(packageNo);
    }

    public void createForOrderPaid(String orderNo) {
        String packageNo = "P" + orderNo;
        if (outboundStore.findByPackageNo(packageNo).isPresent()) {
            return;
        }
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
        create(packageNo, body);
    }

    public record CreateResult(boolean conflict, String outboundNo, HttpStatus httpStatus) {
        static CreateResult created(String outboundNo) {
            return new CreateResult(false, outboundNo, HttpStatus.CREATED);
        }

        static CreateResult conflict(String outboundNo) {
            return new CreateResult(true, outboundNo, HttpStatus.CONFLICT);
        }
    }
}
