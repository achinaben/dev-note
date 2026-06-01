package com.scm.oms.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class OrderRecord {
    private String orderNo;
    private String tradeNo;
    private String buyerId;
    private String clientToken;
    private OrderStatus status;
    private BigDecimal payAmount;
    private OffsetDateTime payTime;
    private int version;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public OffsetDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(OffsetDateTime payTime) {
        this.payTime = payTime;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
