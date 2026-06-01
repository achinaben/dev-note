package com.scm.tms.freight;

import com.scm.tms.integration.CarrierGateway;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FreightApplicationService {
    private final CarrierGateway carrierGateway;

    public FreightApplicationService(CarrierGateway carrierGateway) {
        this.carrierGateway = carrierGateway;
    }

    public Map<String, Object> estimate(Map<String, Object> body) {
        List<Map<String, Object>> options = carrierGateway.quote(body);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("options", options);
        pickCheapest(options).ifPresent(best -> data.put("recommended", best));
        return data;
    }

    @SuppressWarnings("unchecked")
    static Optional<Map<String, Object>> pickCheapest(List<Map<String, Object>> options) {
        return options.stream()
                .min(Comparator.comparingInt(FreightApplicationService::amountMinor));
    }

    private static int amountMinor(Map<String, Object> option) {
        Object v = option.get("amount_minor");
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.MAX_VALUE;
    }
}
