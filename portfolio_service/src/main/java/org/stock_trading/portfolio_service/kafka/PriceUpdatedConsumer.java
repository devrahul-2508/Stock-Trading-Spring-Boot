package org.stock_trading.portfolio_service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.stock_trading.portfolio_service.event.PriceUpdatedEvent;
import org.stock_trading.portfolio_service.service.PortfolioPriceCacheService;
import org.stock_trading.portfolio_service.service.PortfolioService;

@Service
@RequiredArgsConstructor
public class PriceUpdatedConsumer {

    private final PortfolioService portfolioService;
    private final PortfolioPriceCacheService priceCacheService;

    @KafkaListener(
            topics = "price-updated",
            groupId = "portfolio-service",
            containerFactory = "priceUpdatedKafkaListenerContainerFactory"
    )
    public void consume(PriceUpdatedEvent event) {

        // Save latest price in Portfolio Redis
        priceCacheService.savePrice(
                event.getSymbol(),
                event.getNewPrice()
        );

        System.out.println(
                "Price updated: " + event
        );

        portfolioService.handlePriceUpdate(event);
    }
}
