package com.scm.oms.api;

import com.scm.common.web.ApiResponse;
import com.scm.oms.integration.ErpCreditClient;
import com.scm.oms.order.OrderApplicationService;
import com.scm.oms.order.OrderRecord;
import com.scm.oms.order.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private final OrderApplicationService orderService;
    private final ErpCreditClient erpCreditClient;

    public OrderController(OrderApplicationService orderService, ErpCreditClient erpCreditClient) {
        this.orderService = orderService;
        this.erpCreditClient = erpCreditClient;
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest req) {
        String requestId = UUID.randomUUID().toString();
        var result = orderService.createOrder(req.buyerId(), req.clientToken(), req.lines());
        OrderRecord order = result.order();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("trade_no", order.getTradeNo());
        data.put("orders", List.of(Map.of(
                "order_no", order.getOrderNo(),
                "pay_amount", order.getPayAmount().toPlainString(),
                "status", order.getStatus().name()
        )));
        if (!result.created()) {
            Map<String, Object> dupData = new LinkedHashMap<>(data);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("OMS_20003", "Idempotent replay", requestId, dupData));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(requestId, data));
    }

    @PostMapping("/orders/b2b")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createB2b(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody B2bOrderRequest req) {
        if (!erpCreditClient.checkCredit(req.partnerId(), req.orderAmount())) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(new ApiResponse<>("ERP_03001", "Credit limit exceeded",
                            UUID.randomUUID().toString(), Map.of("allowed", false)));
        }
        return create(idempotencyKey, new CreateOrderRequest(
                req.clientToken(), req.buyerId(), "B2B", req.addressId(), req.lines()));
    }

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<Map<String, Object>> get(@PathVariable("orderNo") String orderNo) {
        OrderRecord o = orderService.get(orderNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "order_no", o.getOrderNo(),
                "status", o.getStatus().name(),
                "status_rank", o.getStatus().rank()
        ));
    }

    public record CreateOrderRequest(
            @NotBlank String clientToken,
            @NotBlank String buyerId,
            String channel,
            String addressId,
            @NotEmpty List<Map<String, String>> lines
    ) {
    }

    public record B2bOrderRequest(
            @NotBlank String clientToken,
            @NotBlank String buyerId,
            @NotBlank String partnerId,
            @NotBlank String orderAmount,
            String addressId,
            @NotEmpty List<Map<String, String>> lines
    ) {
    }
}
