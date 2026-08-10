package org.stock_trading.order_service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderPlacedConsumer {

    @KafkaListener(
            topics = "order-placed",
            groupId = "order-service"
    )
    public void consume(OrderPlacedEvent event) {

        log.info(
                "OrderPlaced received: orderId={}, userId={}, symbol={}, type={}, quantity={}, price={}",
                event.getOrderId(),
                event.getUserId(),
                event.getSymbol(),
                event.getOrderType(),
                event.getQuantity(),
                event.getPrice()
        );
    }
}
