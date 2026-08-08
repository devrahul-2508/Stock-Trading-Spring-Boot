package org.stock_trading.market_service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PriceUpdatedConsumer {

    @KafkaListener(
            topics = "price-updated",
            groupId = "market-service"
    )
    public void consumePriceUpdated(PriceUpdatedEvent event) {

        log.info(
                "Price Updated Event received: symbol={}, oldPrice={}, newPrice={}",
                event.getSymbol(),
                event.getOldPrice(),
                event.getNewPrice()
        );
    }
}
