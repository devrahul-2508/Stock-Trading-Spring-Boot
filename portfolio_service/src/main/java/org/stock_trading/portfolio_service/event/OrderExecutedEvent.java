package org.stock_trading.portfolio_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderExecutedEvent {

    private Long orderId;

    private Long userId;

    private String symbol;

    private String orderType;

    private Integer quantity;

    private BigDecimal executionPrice;

    private LocalDateTime executedAt;
}
