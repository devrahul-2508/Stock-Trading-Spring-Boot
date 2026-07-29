package org.stock_trading.market_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.stock_trading.market_service.dto.StockResponse;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, StockResponse> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, StockResponse> template = new RedisTemplate<>();
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