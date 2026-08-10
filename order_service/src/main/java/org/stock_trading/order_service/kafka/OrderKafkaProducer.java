package org.stock_trading.order_service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.stock_trading.order_service.event.OrderPlacedEvent;

@Service
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private static final String ORDER_PLACED_TOPIC = "order-placed";

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void pushOrderPlaced(OrderPlacedEvent event){
        kafkaTemplate.send(
                ORDER_PLACED_TOPIC,
                event.getOrderId().toString(),
                event
        );
    }
}
