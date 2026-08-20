package org.stock_trading.order_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.stock_trading.order_service.service.OrderService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedConsumer {

    private final OrderService orderService;

    @KafkaListener(
            topics = "order-placed",
            groupId = "order-execution-service"
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

        orderService.executeOrder(event.getOrderId());

    }
}
