package org.stock_trading.portfolio_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TradeResponse {

    private Long id;
    private Long orderId;
    private String symbol;
    private String orderType;
    private Integer quantity;
    private BigDecimal executionPrice;
    private BigDecimal realizedProfitLoss;
    private LocalDateTime executedAt;
}