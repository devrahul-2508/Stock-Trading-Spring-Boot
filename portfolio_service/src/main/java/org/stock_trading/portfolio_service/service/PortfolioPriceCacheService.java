
package org.stock_trading.portfolio_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PortfolioPriceCacheService {

    private final RedisTemplate<String, BigDecimal> redisTemplate;

    public PortfolioPriceCacheService(
            @Qualifier("portfolioRedisTemplate")
            RedisTemplate<String, BigDecimal> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    public void savePrice(String symbol, BigDecimal price) {

        redisTemplate.opsForValue().set(
                "price:" + symbol.toUpperCase(),
                price
        );
    }

    public BigDecimal getPrice(String symbol) {

        return redisTemplate.opsForValue().get(
                "price:" + symbol.toUpperCase()
        );
    }
}

