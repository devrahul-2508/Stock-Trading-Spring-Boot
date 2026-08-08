package org.stock_trading.order_service.dto;

import lombok.Builder;
import lombok.Data;
import org.stock_trading.order_service.enums.OrderStatus;
import org.stock_trading.order_service.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {

    private Long id;

    private Long userId;

    private String symbol;

    private OrderType orderType;

    private Integer quantity;

    private BigDecimal price;

    private OrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime executedAt;
}
