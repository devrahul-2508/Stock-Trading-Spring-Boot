package org.stock_trading.market_service.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdatedEvent {

    private String symbol;

    private BigDecimal oldPrice;

    private BigDecimal newPrice;

    private LocalDateTime updatedAt;
}
