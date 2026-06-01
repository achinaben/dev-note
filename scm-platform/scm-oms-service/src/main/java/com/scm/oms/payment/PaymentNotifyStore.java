package com.scm.oms.payment;

public interface PaymentNotifyStore {
    void recordSuccess(String orderNo, String notifyId);

    int successCount(String orderNo);
}
