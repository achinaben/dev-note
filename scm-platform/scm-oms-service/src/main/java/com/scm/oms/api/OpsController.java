package com.scm.oms.api;

import com.scm.common.web.ApiResponse;
import com.scm.oms.aftersale.AfterSaleService;
import com.scm.oms.fulfillment.FulfillmentService;
import com.scm.oms.integration.TmsFulfillmentClient;
import com.scm.oms.fulfillment.PackageRepository;
import com.scm.oms.order.OrderApplicationService;
import com.scm.oms.payment.PaymentNotifyStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/** E2E / 联调辅助接口，生产应关闭或加鉴权 */
@RestController
@RequestMapping("/api/v1/ops")
public class OpsController {
    private final FulfillmentService fulfillmentService;
    private final OrderApplicationService orderService;
    private final PackageRepository packageRepository;
    private final TmsFulfillmentClient tmsClient;
    private final PaymentNotifyStore paymentNotifyStore;
    private final AfterSaleService afterSaleService;

    public OpsController(
            FulfillmentService fulfillmentService,
            OrderApplicationService orderService,
            PackageRepository packageRepository,
            TmsFulfillmentClient tmsClient,
            PaymentNotifyStore paymentNotifyStore,
            AfterSaleService afterSaleService) {
        this.fulfillmentService = fulfillmentService;
        this.orderService = orderService;
        this.packageRepository = packageRepository;
        this.tmsClient = tmsClient;
        this.paymentNotifyStore = paymentNotifyStore;
        this.afterSaleService = afterSaleService;
    }

    @PostMapping("/orders/{orderNo}/ship")
    public ApiResponse<Map<String, String>> ship(@PathVariable("orderNo") String orderNo) {
        fulfillmentService.markShipped(orderNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of("status", "SHIPPED"));
    }

    @PostMapping("/orders/{orderNo}/deliver")
    public ApiResponse<Map<String, String>> deliver(@PathVariable("orderNo") String orderNo) {
        var pkg = packageRepository.findByOrderNo(orderNo).orElseThrow();
        tmsClient.createAndDeliver(pkg.getPackageNo(), orderNo);
        fulfillmentService.markDelivered(orderNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of("status", "DELIVERED"));
    }

    @GetMapping("/orders/trade-count")
    public ApiResponse<Map<String, Object>> tradeCount(
            @RequestParam("buyer_id") String buyerId,
            @RequestParam("client_token") String clientToken) {
        long count = orderService.countTradeOrders(buyerId, clientToken);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "buyer_id", buyerId,
                "client_token", clientToken,
                "trade_order_count", count
        ));
    }

    @GetMapping("/orders/{orderNo}/diag")
    public ApiResponse<Map<String, Object>> diag(@PathVariable("orderNo") String orderNo) {
        var order = orderService.get(orderNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "status", order.getStatus().name(),
                "inventory", orderService.inventoryStatus(orderNo),
                "order_paid_event", orderService.hasOrderPaidEvent(orderNo),
                "payment_success_count", paymentSuccessCount(orderNo)
        ));
    }

    @PostMapping("/orders/{orderNo}/close-expired")
    public ApiResponse<Map<String, String>> closeExpired(@PathVariable("orderNo") String orderNo) {
        var o = orderService.closeUnpaid(orderNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of("status", o.getStatus().name()));
    }

    @PostMapping("/orders/{orderNo}/packages/init-two")
    public ApiResponse<Map<String, Object>> initTwoPackages(@PathVariable("orderNo") String orderNo) {
        var packages = fulfillmentService.initTwoPackages(orderNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "count", packages.size(),
                "package_nos", packages.stream().map(p -> p.getPackageNo()).toList()
        ));
    }

    @PostMapping("/orders/{orderNo}/packages/{packageNo}/ship")
    public ApiResponse<Map<String, String>> shipPackage(
            @PathVariable("orderNo") String orderNo,
            @PathVariable("packageNo") String packageNo) {
        fulfillmentService.shipPackage(orderNo, packageNo);
        var order = orderService.get(orderNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of("status", order.getStatus().name()));
    }

    @PostMapping("/after-sale/{afterSaleNo}/approve")
    public ApiResponse<Map<String, String>> approveAfterSale(@PathVariable("afterSaleNo") String afterSaleNo) {
        var r = afterSaleService.approve(afterSaleNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of("status", r.getStatus()));
    }

    @PostMapping("/orders/{orderNo}/refund-success")
    public ApiResponse<Map<String, String>> refundSuccess(@PathVariable("orderNo") String orderNo) {
        var r = afterSaleService.completeRefund(orderNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "after_sale_status", r.getStatus()
        ));
    }

    @GetMapping("/orders/{orderNo}/after-sale/diag")
    public ApiResponse<Map<String, Object>> afterSaleDiag(@PathVariable("orderNo") String orderNo) {
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "after_sale_status", afterSaleService.status(orderNo),
                "refund_completed_event", afterSaleService.hasRefundCompletedEvent(orderNo)
        ));
    }

    private int paymentSuccessCount(String orderNo) {
        return paymentNotifyStore.successCount(orderNo);
    }
}
