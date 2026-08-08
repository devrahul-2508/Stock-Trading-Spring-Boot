package org.stock_trading.order_service.controller;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.stock_trading.order_service.dto.CreateOrderRequest;
import org.stock_trading.order_service.dto.OrderResponse;
import org.stock_trading.order_service.entity.Order;
import org.stock_trading.order_service.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
           @RequestParam Long userId,
           @Valid @RequestBody CreateOrderRequest request

    ){
        return ResponseEntity.ok(orderService.createOrder(userId,request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
           @RequestParam Long userId
    ){
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }
}
