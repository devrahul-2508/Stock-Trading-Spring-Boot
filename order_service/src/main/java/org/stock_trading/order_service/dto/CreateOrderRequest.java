package org.stock_trading.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.stock_trading.order_service.enums.OrderType;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {

    @NotBlank
    private String symbol;

    @NotNull
    private OrderType orderType;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private BigDecimal price;
}
