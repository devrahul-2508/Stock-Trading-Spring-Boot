package org.stock_trading.portfolio_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {

    private Long userId;

    private List<HoldingResponse> holdings;

    private BigDecimal totalInvested;

    private BigDecimal currentValue;

    private BigDecimal profitLoss;
}
