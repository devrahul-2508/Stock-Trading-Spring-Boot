package org.stock_trading.order_service.controller;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.stock_trading.order_service.dto.CreateOrderRequest;
import org.stock_trading.order_service.dto.OrderResponse;
import org.stock_trading.order_service.entity.Order;
import org.stock_trading.order_service.enums.OrderStatus;
import org.stock_trading.order_service.enums.OrderType;
import org.stock_trading.order_service.service.OrderService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication,
           @Valid @RequestBody CreateOrderRequest request

    ){
        Long userId = (Long) authentication
                .getPrincipal();

        return ResponseEntity.ok(orderService.createOrder(userId,request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) String symbol
    ){
        Long userId = (Long) authentication
                .getPrincipal();

        return ResponseEntity.ok(  orderService.getUserOrders(
                userId,
                status,
                orderType,
                symbol
        ));
    }
}
