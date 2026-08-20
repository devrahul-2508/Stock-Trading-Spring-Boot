package org.stock_trading.portfolio_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.stock_trading.portfolio_service.entity.Holding;
import org.stock_trading.portfolio_service.event.OrderExecutedEvent;
import org.stock_trading.portfolio_service.repository.HoldingRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;

    @Transactional
    public void processOrder(OrderExecutedEvent event) {

        if ("BUY".equalsIgnoreCase(event.getOrderType())) {

            processBuy(event);

        } else if ("SELL".equalsIgnoreCase(event.getOrderType())) {

            processSell(event);

        } else {

            throw new IllegalArgumentException(
                    "Unsupported order type: "
                            + event.getOrderType()
            );
        }
    }

    private void processBuy(OrderExecutedEvent event) {

        Holding holding =
                holdingRepository
                        .findByUserIdAndSymbol(
                                event.getUserId(),
                                event.getSymbol()
                        )
                        .orElse(null);

        BigDecimal executionValue =
                event.getExecutionPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        event.getQuantity()
                                )
                        );

        if (holding == null) {

            holding = Holding.builder()
                    .userId(event.getUserId())
                    .symbol(event.getSymbol())
                    .quantity(event.getQuantity())
                    .averageBuyPrice(event.getExecutionPrice())
                    .investedAmount(executionValue)
                    .build();

        } else {

            int oldQuantity = holding.getQuantity();

            BigDecimal oldInvested =
                    holding.getInvestedAmount();

            int newQuantity =
                    oldQuantity + event.getQuantity();

            BigDecimal newInvested =
                    oldInvested.add(executionValue);

            BigDecimal newAveragePrice =
                    newInvested.divide(
                            BigDecimal.valueOf(newQuantity),
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            holding.setQuantity(newQuantity);
            holding.setInvestedAmount(newInvested);
            holding.setAverageBuyPrice(newAveragePrice);
        }

        holdingRepository.save(holding);
    }

    private void processSell(OrderExecutedEvent event) {

        Holding holding =
                holdingRepository
                        .findByUserIdAndSymbol(
                                event.getUserId(),
                                event.getSymbol()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Holding not found"
                                ));

        if (holding.getQuantity()
                < event.getQuantity()) {

            throw new RuntimeException(
                    "Insufficient quantity"
            );
        }

        int remainingQuantity =
                holding.getQuantity()
                        - event.getQuantity();

        if (remainingQuantity == 0) {

            holdingRepository.delete(holding);

        } else {

            BigDecimal remainingInvested =
                    holding.getAverageBuyPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            remainingQuantity
                                    )
                            );

            holding.setQuantity(remainingQuantity);
            holding.setInvestedAmount(
                    remainingInvested
            );

            holdingRepository.save(holding);
        }
    }
}
