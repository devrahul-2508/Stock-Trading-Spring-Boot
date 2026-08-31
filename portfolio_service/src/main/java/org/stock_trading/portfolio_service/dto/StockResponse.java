package org.stock_trading.portfolio_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {

    private Long id;

    private String symbol;

    private String companyName;

    private BigDecimal price;
}
