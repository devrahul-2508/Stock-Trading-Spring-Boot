package org.stock_trading.portfolio_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.stock_trading.portfolio_service.event.OrderExecutedEvent;
import org.stock_trading.portfolio_service.event.PriceUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private Map<String, Object> consumerProperties() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        return props;
    }

    // =========================
    // Price Updated
    // =========================

    @Bean
    public ConsumerFactory<String, PriceUpdatedEvent>
    priceUpdatedConsumerFactory() {

        JacksonJsonDeserializer<PriceUpdatedEvent> deserializer =
                new JacksonJsonDeserializer<>(PriceUpdatedEvent.class);

        // Ignore __TypeId__ sent by market-service
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PriceUpdatedEvent>
    priceUpdatedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, PriceUpdatedEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(priceUpdatedConsumerFactory());

        return factory;
    }


    // =========================
    // Order Executed
    // =========================

    @Bean
    public ConsumerFactory<String, OrderExecutedEvent>
    orderExecutedConsumerFactory() {

        JacksonJsonDeserializer<OrderExecutedEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderExecutedEvent.class);

        // Ignore __TypeId__ sent by order-service
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderExecutedEvent>
    orderExecutedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderExecutedEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderExecutedConsumerFactory());

        return factory;
    }
}