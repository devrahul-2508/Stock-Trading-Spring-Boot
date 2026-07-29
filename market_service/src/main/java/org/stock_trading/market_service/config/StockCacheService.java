package org.stock_trading.market_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.stock_trading.market_service.dto.StockResponse;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class StockCacheService {


    private final RedisTemplate<String, StockResponse> redisTemplate;

    public void save(String symbol, StockResponse stock) {

        redisTemplate.opsForValue()
                .set("stock:" + symbol, stock, Duration.ofMinutes(5));
    }

    public StockResponse get(String symbol) {

        return redisTemplate.opsForValue()
                .get("stock:" + symbol);
    }

    public void delete(String symbol) {

        redisTemplate.delete("stock:" + symbol);
    }

}
