package com.scm.erp.api;

import com.scm.common.web.ApiResponse;
import com.scm.erp.fi.JournalRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integration/journal")
public class JournalQueryController {
    private final JournalRepository journalRepository;

    public JournalQueryController(JournalRepository journalRepository) {
        this.journalRepository = journalRepository;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> byBizKey(@RequestParam("biz_key") String bizKey) {
        var je = journalRepository.findByBizKey(bizKey)
                .orElseThrow(() -> new IllegalArgumentException("ERP_10002"));
        java.util.LinkedHashMap<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("je_no", je.getJeNo());
        data.put("biz_key", je.getBizKey());
        data.put("status", je.getStatus());
        if (je.getWaybillNo() != null) {
            data.put("waybill_no", je.getWaybillNo());
        }
        return ApiResponse.ok(UUID.randomUUID().toString(), data);
    }
}
