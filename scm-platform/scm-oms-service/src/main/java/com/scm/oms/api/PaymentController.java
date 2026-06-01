package com.scm.oms.api;

import com.scm.oms.order.OrderApplicationService;
import com.scm.oms.order.OrderRecord;
import com.scm.oms.payment.PaymentNotifyStore;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/payments/notify")
public class PaymentController {
    private final OrderApplicationService orderService;
    private final PaymentNotifyStore paymentNotifyStore;
    private final Set<String> processedNotify = ConcurrentHashMap.newKeySet();

    public PaymentController(OrderApplicationService orderService, PaymentNotifyStore paymentNotifyStore) {
        this.orderService = orderService;
        this.paymentNotifyStore = paymentNotifyStore;
    }

    @PostMapping("/{channel}")
    public String notify(@PathVariable String channel, @RequestBody PayNotifyRequest req) {
        String key = channel + "::" + req.notifyId();
        if (!processedNotify.add(key)) {
            return "SUCCESS";
        }
        OrderRecord o = orderService.markPaid(req.orderNo(), req.notifyId());
        paymentNotifyStore.recordSuccess(req.orderNo(), req.notifyId());
        return "SUCCESS";
    }

    public record PayNotifyRequest(String notifyId, String orderNo, String outTradeNo, long amountMinor, boolean signVerified) {
    }
}
