package com.scm.erp.integration;

import com.scm.common.idempotency.ProcessedMessageStore;
import com.scm.erp.fi.JournalEntryRecord;
import com.scm.erp.fi.JournalRepository;
import com.scm.erp.fi.PostingEngine;
import com.scm.erp.inventory.InventoryLedgerRecord;
import com.scm.erp.inventory.InventoryLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WmsShipmentPostingService {
    private static final String CONSUMER_GROUP = "erp-wms-cg";

    private final ProcessedMessageStore processedMessageStore;
    private final PostingEngine postingEngine;
    private final JournalRepository journalRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final FiscalPeriodService fiscalPeriodService;

    public WmsShipmentPostingService(
            ProcessedMessageStore processedMessageStore,
            PostingEngine postingEngine,
            JournalRepository journalRepository,
            InventoryLedgerRepository inventoryLedgerRepository,
            FiscalPeriodService fiscalPeriodService) {
        this.processedMessageStore = processedMessageStore;
        this.postingEngine = postingEngine;
        this.journalRepository = journalRepository;
        this.inventoryLedgerRepository = inventoryLedgerRepository;
        this.fiscalPeriodService = fiscalPeriodService;
    }

    public PostingResult postShipment(String bizKey, WmsShipmentRequest req) {
        fiscalPeriodService.assertOpen(req.orgId());
        if (processedMessageStore.exists(bizKey, CONSUMER_GROUP)) {
            JournalEntryRecord existing = journalRepository.findByBizKey(bizKey).orElseThrow();
            return PostingResult.idempotent(existing.getJeNo(), existing.getBizKey());
        }
        BigDecimal cost = postingEngine.computeOutboundCost(req.lines());
        applyInventory(req.orgId(), req.whCode(), req.lines(), cost);
        JournalEntryRecord je = postingEngine.postOutboundCost(bizKey, cost);
        if (req.waybillNo() != null && !req.waybillNo().isBlank()) {
            je.setWaybillNo(req.waybillNo());
        }
        journalRepository.save(je);
        processedMessageStore.markProcessed(bizKey, CONSUMER_GROUP, req.outboundNo());
        return PostingResult.created(je.getJeNo(), je.getBizKey(), cost);
    }

    private void applyInventory(String orgId, String whCode, List<Map<String, String>> lines, BigDecimal totalCost) {
        for (Map<String, String> line : lines) {
            InventoryLedgerRecord ledger = inventoryLedgerRepository.getOrCreate(
                    orgId, whCode, line.get("material_code"));
            BigDecimal qty = new BigDecimal(line.get("qty"));
            ledger.setQtyOnHand(ledger.getQtyOnHand().subtract(qty));
        }
        if (lines.size() == 1) {
            InventoryLedgerRecord ledger = inventoryLedgerRepository.getOrCreate(
                    orgId, whCode, lines.get(0).get("material_code"));
            ledger.setAmountOnHand(ledger.getAmountOnHand().subtract(totalCost));
        }
    }

    public record WmsShipmentRequest(
            String outboundNo,
            String sourceOrderNo,
            String orgId,
            String whCode,
            List<Map<String, String>> lines,
            String waybillNo
    ) {
        public WmsShipmentRequest(
                String outboundNo,
                String sourceOrderNo,
                String orgId,
                String whCode,
                List<Map<String, String>> lines) {
            this(outboundNo, sourceOrderNo, orgId, whCode, lines, null);
        }
    }

    public record PostingResult(boolean idempotentHit, String jeNo, String erpTxnNo, BigDecimal cost) {
        static PostingResult created(String jeNo, String bizKey, BigDecimal cost) {
            return new PostingResult(false, jeNo, "ITX-" + jeNo, cost);
        }

        static PostingResult idempotent(String jeNo, String bizKey) {
            return new PostingResult(true, jeNo, "ITX-" + jeNo, null);
        }
    }
}
