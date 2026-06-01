package com.scm.erp.inventory;

import java.math.BigDecimal;

public class InventoryLedgerRecord {
    private String orgId;
    private String whCode;
    private String materialCode;
    private BigDecimal qtyOnHand = BigDecimal.ZERO;
    private BigDecimal amountOnHand = BigDecimal.ZERO;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getWhCode() {
        return whCode;
    }

    public void setWhCode(String whCode) {
        this.whCode = whCode;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public BigDecimal getQtyOnHand() {
        return qtyOnHand;
    }

    public void setQtyOnHand(BigDecimal qtyOnHand) {
        this.qtyOnHand = qtyOnHand;
    }

    public BigDecimal getAmountOnHand() {
        return amountOnHand;
    }

    public void setAmountOnHand(BigDecimal amountOnHand) {
        this.amountOnHand = amountOnHand;
    }
}
