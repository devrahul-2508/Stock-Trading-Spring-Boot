package org.stock_trading.market_service.dto;


import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePriceRequest {

    @DecimalMin("0.01")
    private BigDecimal currentPrice;
}
