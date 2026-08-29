package org.stock_trading.portfolio_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingResponse {
    private Long id;

    private Long userId;

    private String symbol;

    private Integer quantity;

    private BigDecimal averageBuyPrice;

    private BigDecimal investedAmount;
}
