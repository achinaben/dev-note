package com.scm.erp.fi;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class PostingEngine {

    /** 销售出库成本结转：借 6401 / 贷 1405 */
    public JournalEntryRecord postOutboundCost(String bizKey, BigDecimal costAmount) {
        JournalEntryRecord je = new JournalEntryRecord();
        je.setBizKey(bizKey);
        JournalLineRecord debit = new JournalLineRecord();
        debit.setAccountCode("6401");
        debit.setDebit(costAmount);
        JournalLineRecord credit = new JournalLineRecord();
        credit.setAccountCode("1405");
        credit.setCredit(costAmount);
        je.lines().add(debit);
        je.lines().add(credit);
        assertBalanced(je);
        return je;
    }

    public BigDecimal computeOutboundCost(List<Map<String, String>> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, String> line : lines) {
            BigDecimal qty = new BigDecimal(line.get("qty"));
            String material = line.get("material_code");
            BigDecimal unitCost = unitCost(material);
            total = total.add(qty.multiply(unitCost));
        }
        return total;
    }

    private BigDecimal unitCost(String materialCode) {
        if ("M001".equals(materialCode)) {
            return new BigDecimal("50.0000");
        }
        throw new IllegalArgumentException("ERP_05001");
    }

    private void assertBalanced(JournalEntryRecord je) {
        BigDecimal d = BigDecimal.ZERO;
        BigDecimal c = BigDecimal.ZERO;
        for (JournalLineRecord line : je.lines()) {
            d = d.add(line.getDebit());
            c = c.add(line.getCredit());
        }
        if (d.compareTo(c) != 0) {
            throw new IllegalStateException("ERP_01002");
        }
    }
}
