package org.stock_trading.portfolio_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import java.math.BigDecimal;

@Configuration
public class PortfolioRedisConfig {

    @Bean
    public RedisTemplate<String, BigDecimal> portfolioRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, BigDecimal> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Key: String
        template.setKeySerializer(
                new StringRedisSerializer()
        );

        // Value: BigDecimal
        template.setValueSerializer(
                new GenericToStringSerializer<>(BigDecimal.class)
        );

        template.setHashKeySerializer(
                new StringRedisSerializer()
        );

        template.setHashValueSerializer(
                new GenericToStringSerializer<>(BigDecimal.class)
        );

        template.afterPropertiesSet();

        return template;
    }
}


