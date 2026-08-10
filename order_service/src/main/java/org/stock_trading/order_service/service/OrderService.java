package org.stock_trading.order_service.service;

import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.stock_trading.order_service.dto.CreateOrderRequest;
import org.stock_trading.order_service.dto.OrderResponse;
import org.stock_trading.order_service.entity.Order;
import org.stock_trading.order_service.enums.OrderStatus;
import org.stock_trading.order_service.event.OrderPlacedEvent;
import org.stock_trading.order_service.kafka.OrderKafkaProducer;
import org.stock_trading.order_service.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderKafkaProducer orderKafkaProducer;

    public OrderResponse createOrder(
            Long userId,
            CreateOrderRequest request
    ){
        Order order = Order.builder()
                .userId(userId)
                .symbol(request.getSymbol().toUpperCase())
                .orderType(request.getOrderType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();


        Order savedOrder = orderRepository.save(order);
        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getSymbol(),
                savedOrder.getOrderType().name(),
                savedOrder.getQuantity(),
                savedOrder.getPrice(),
                savedOrder.getCreatedAt()
        );

        orderKafkaProducer.pushOrderPlaced(event);

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getUserOrders(Long userId){

        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    OrderResponse mapToResponse(Order order){
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .symbol(order.getSymbol())
                .orderType(order.getOrderType())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .executedAt(order.getExecutedAt())
                .build();
    }
}
