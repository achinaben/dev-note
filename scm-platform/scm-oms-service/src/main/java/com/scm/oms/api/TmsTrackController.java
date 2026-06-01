package com.scm.oms.api;

import com.scm.common.web.ApiResponse;
import com.scm.oms.fulfillment.FulfillmentService;
import com.scm.oms.order.OrderRepository;
import com.scm.oms.order.OrderStateMachine;
import com.scm.oms.order.OrderStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integration/tms")
public class TmsTrackController {
    private final OrderRepository orderRepository;
    private final OrderStateMachine stateMachine;
    private final FulfillmentService fulfillmentService;

    public TmsTrackController(
            OrderRepository orderRepository,
            OrderStateMachine stateMachine,
            FulfillmentService fulfillmentService) {
        this.orderRepository = orderRepository;
        this.stateMachine = stateMachine;
        this.fulfillmentService = fulfillmentService;
    }

    @PostMapping("/track")
    public ApiResponse<Map<String, String>> track(@RequestBody Map<String, String> body) {
        String orderNo = body.get("order_no");
        String event = body.get("event");
        var order = orderRepository.findByOrderNo(orderNo).orElseThrow();
        OrderStatus target = mapEvent(event);
        if (target != null && order.getStatus().rank() < target.rank()) {
            order.setStatus(stateMachine.apply(order.getStatus(), target));
            orderRepository.update(order);
        }
        if (target == OrderStatus.DELIVERED) {
            fulfillmentService.markDelivered(orderNo);
        }
        return ApiResponse.ok(UUID.randomUUID().toString(),
                Map.of("status", orderRepository.findByOrderNo(orderNo).orElseThrow().getStatus().name()));
    }

    private static OrderStatus mapEvent(String event) {
        return switch (event) {
            case "TMS_IN_TRANSIT" -> OrderStatus.SHIPPED;
            case "TMS_DELIVERED" -> OrderStatus.DELIVERED;
            default -> null;
        };
    }
}
