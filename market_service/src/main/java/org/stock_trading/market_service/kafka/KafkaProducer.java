package org.stock_trading.market_service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.stock_trading.market_service.event.PriceUpdatedEvent;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private static final String PRICE_UPDATED_TOPIC = "price-updated";

    private final KafkaTemplate<String, PriceUpdatedEvent> kafkaTemplate;

    public void publishPriceUpdated(PriceUpdatedEvent event) {

        kafkaTemplate.send(
                PRICE_UPDATED_TOPIC,
                event.getSymbol(),
                event
        );
    }
}