package org.stock_trading.portfolio_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.RedisTemplate;
import org.stock_trading.portfolio_service.dto.HoldingResponse;
import org.stock_trading.portfolio_service.dto.PortfolioResponse;
import org.stock_trading.portfolio_service.dto.StockResponse;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, PortfolioResponse> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, PortfolioResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        JacksonJsonRedisSerializer<PortfolioResponse> serializer =
                new JacksonJsonRedisSerializer<>(PortfolioResponse.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, StockResponse> stockRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, StockResponse> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        JacksonJsonRedisSerializer<StockResponse> serializer =
                new JacksonJsonRedisSerializer<>(StockResponse.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }
}