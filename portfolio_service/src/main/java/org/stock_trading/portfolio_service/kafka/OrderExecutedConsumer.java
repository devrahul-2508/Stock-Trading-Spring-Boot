package org.stock_trading.portfolio_service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.stock_trading.portfolio_service.event.OrderExecutedEvent;
import org.stock_trading.portfolio_service.service.PortfolioService;

@Component
@RequiredArgsConstructor
public class OrderExecutedConsumer {

    private final PortfolioService portfolioService;
    @KafkaListener(
            topics = "order-executed",
            groupId = "portfolio-order-service",
            containerFactory = "orderExecutedKafkaListenerContainerFactory"
    )
    public void consume(OrderExecutedEvent event) {

        System.out.println(
                "Order executed received: " + event
        );

        // Next:
        // update/create holding
        portfolioService.processOrder(event);

    }
}