package org.stock_trading.market_service.dto;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StockResponse {
    private Long id;

    private String symbol;

    private String companyName;

    private BigDecimal currentPrice;

    private String exchange;

    private String sector;
}
