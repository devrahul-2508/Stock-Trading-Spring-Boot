package org.stock_trading.portfolio_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.stock_trading.portfolio_service.dto.StockResponse;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MarketPriceCacheService {

    private final RedisTemplate<String, StockResponse>
            stockRedisTemplate;

    public BigDecimal getPrice(String symbol) {

        String key =
                "stock:" + symbol.toUpperCase();

        StockResponse stock =
                stockRedisTemplate
                        .opsForValue()
                        .get(key);

        if (stock == null) {
            return null;
        }

        return stock.getPrice();
    }
}