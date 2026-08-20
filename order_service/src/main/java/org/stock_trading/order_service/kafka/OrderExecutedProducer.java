package org.stock_trading.order_service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.stock_trading.order_service.event.OrderExecutedEvent;

@Service
@RequiredArgsConstructor
public class OrderExecutedProducer {

    private static final String TOPIC = "order-executed";

    private final KafkaTemplate<String, OrderExecutedEvent> kafkaTemplate;

    public void publish(OrderExecutedEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getOrderId().toString(),
                event
        );
    }
}
