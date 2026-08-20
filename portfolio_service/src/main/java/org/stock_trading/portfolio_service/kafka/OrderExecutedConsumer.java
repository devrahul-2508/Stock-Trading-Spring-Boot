package org.stock_trading.portfolio_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.stock_trading.portfolio_service.event.OrderExecutedEvent;
import org.stock_trading.portfolio_service.service.PortfolioService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExecutedConsumer {

    private final PortfolioService portfolioService;

    @KafkaListener(
            topics = "order-executed",
            groupId = "portfolio-service"
    )
    public void consume(OrderExecutedEvent event){
        log.info(
                "OrderExecuted received: orderId={}, userId={}, symbol={}",
                event.getOrderId(),
                event.getUserId(),
                event.getSymbol()
        );
        portfolioService.processOrder(event);
    }
}
