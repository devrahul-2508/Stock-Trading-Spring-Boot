package org.stock_trading.portfolio_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.stock_trading.portfolio_service.dto.PortfolioResponse;

@Service
@RequiredArgsConstructor
public class PortfolioCacheService {

    private final RedisTemplate<String, PortfolioResponse>
            redisTemplate;

    public void save(
            Long userId,
            PortfolioResponse portfolio) {

        redisTemplate.opsForValue().set(
                buildKey(userId),
                portfolio
        );
    }

    public PortfolioResponse get(Long userId) {

        return redisTemplate.opsForValue()
                .get(buildKey(userId));
    }

    public void delete(Long userId) {

        redisTemplate.delete(
                buildKey(userId)
        );
    }

    private String buildKey(Long userId) {

        return "portfolio:" + userId;
    }
}