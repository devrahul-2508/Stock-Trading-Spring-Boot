package org.stock_trading.market_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateStockRequest {

    @NotBlank
    private String symbol;

    @NotBlank
    private String companyName;

    @DecimalMin("0.01")
    private BigDecimal currentPrice;

    @NotBlank
    private String exchange;

    @NotBlank
    private String sector;

}
