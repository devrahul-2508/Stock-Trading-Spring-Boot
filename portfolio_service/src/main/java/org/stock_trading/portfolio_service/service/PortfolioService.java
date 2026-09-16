package org.stock_trading.portfolio_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.stock_trading.portfolio_service.dto.HoldingResponse;
import org.stock_trading.portfolio_service.dto.PortfolioResponse;
import org.stock_trading.portfolio_service.dto.TradeResponse;
import org.stock_trading.portfolio_service.entity.Holding;
import org.stock_trading.portfolio_service.entity.ProcessedOrder;
import org.stock_trading.portfolio_service.entity.Trade;
import org.stock_trading.portfolio_service.event.OrderExecutedEvent;
import org.stock_trading.portfolio_service.event.PriceUpdatedEvent;
import org.stock_trading.portfolio_service.repository.HoldingRepository;
import org.stock_trading.portfolio_service.repository.ProcessedOrderRepository;
import org.stock_trading.portfolio_service.repository.TradeRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;

    private final PortfolioCacheService cacheService;

    private final PortfolioPriceCacheService portfolioPriceCacheService;

    private final ProcessedOrderRepository processedOrderRepository;

    private final TradeRepository tradeRepository;


    // ============================================================
    // ORDER EXECUTION
    // ============================================================

    @Transactional
    public void processOrder(OrderExecutedEvent event) {

        if (processedOrderRepository.existsById(event.getOrderId())) {

            System.out.println(
                    "Order already processed: "
                            + event.getOrderId()
            );

            return;
        }

        BigDecimal realizedProfitLoss = BigDecimal.ZERO;

        if ("BUY".equalsIgnoreCase(event.getOrderType())) {

            processBuy(event);

        } else if ("SELL".equalsIgnoreCase(event.getOrderType())) {

            realizedProfitLoss = processSell(event);

        } else {

            throw new IllegalArgumentException(
                    "Unsupported order type: "
                            + event.getOrderType()
            );
        }

        Trade trade = Trade.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .symbol(event.getSymbol().toUpperCase())
                .orderType(event.getOrderType())
                .quantity(event.getQuantity())
                .executionPrice(event.getExecutionPrice())
                .realizedProfitLoss(realizedProfitLoss)
                .executedAt(event.getExecutedAt())
                .build();

        tradeRepository.save(trade);

        processedOrderRepository.save(
                new ProcessedOrder(event.getOrderId())
        );
    }


    // ============================================================
    // BUY
    // ============================================================

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
                    .symbol(event.getSymbol().toUpperCase())
                    .quantity(event.getQuantity())
                    .averageBuyPrice(event.getExecutionPrice())
                    .investedAmount(executionValue)
                    .build();

        } else {

            int oldQuantity =
                    holding.getQuantity();

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

            holding.setInvestedAmount(
                    newInvested
            );

            holding.setAverageBuyPrice(
                    newAveragePrice
            );
        }


        holdingRepository.save(holding);


        /*
         * The holding has changed.
         *
         * The existing cached portfolio is now stale.
         * Delete it and let the next GET rebuild it from DB.
         */
        invalidatePortfolioCache(
                event.getUserId()
        );
    }


    // ============================================================
    // SELL
    // ============================================================

    private BigDecimal processSell(OrderExecutedEvent event) {

        Holding holding =
                holdingRepository
                        .findByUserIdAndSymbol(
                                event.getUserId(),
                                event.getSymbol()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Holding not found"
                                )
                        );

        if (holding.getQuantity() < event.getQuantity()) {

            throw new RuntimeException(
                    "Insufficient quantity"
            );
        }

        BigDecimal averageBuyPrice =
                holding.getAverageBuyPrice();

        BigDecimal realizedProfitLoss =
                event.getExecutionPrice()
                        .subtract(averageBuyPrice)
                        .multiply(
                                BigDecimal.valueOf(
                                        event.getQuantity()
                                )
                        );

        int remainingQuantity =
                holding.getQuantity()
                        - event.getQuantity();

        if (remainingQuantity == 0) {

            holdingRepository.delete(holding);

        } else {

            BigDecimal remainingInvested =
                    averageBuyPrice.multiply(
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

        invalidatePortfolioCache(
                event.getUserId()
        );

        return realizedProfitLoss;
    }


    // ============================================================
    // GET PORTFOLIO
    // ============================================================

    public PortfolioResponse getPortfolio(Long userId) {

        /*
         * First try Redis.
         */
        try {

            PortfolioResponse cached =
                    cacheService.get(userId);

            if (cached != null) {

                System.out.println(
                        "Fetching portfolio from Redis"
                );

                return cached;
            }

        } catch (Exception e) {

            /*
             * Redis is only a cache.
             *
             * If Redis is down, do NOT fail the API.
             * Continue to PostgreSQL.
             */

            System.out.println(
                    "Redis unavailable. Fetching portfolio from DB."
            );
        }


        /*
         * Redis MISS.
         *
         * Fetch from PostgreSQL.
         */
        System.out.println(
                "Fetching portfolio from PostgreSQL"
        );


        PortfolioResponse response =
                calculatePortfolio(userId);


        /*
         * Try to populate Redis.
         *
         * Redis failure should not break the API.
         */
        try {

            cacheService.save(
                    userId,
                    response
            );

        } catch (Exception e) {

            System.out.println(
                    "Could not save portfolio to Redis: "
                            + e.getMessage()
            );
        }


        return response;
    }


    // ============================================================
    // BUILD PORTFOLIO
    // ============================================================

    public PortfolioResponse calculatePortfolio(
            Long userId) {

        List<Holding> holdings =
                holdingRepository.findByUserId(userId);


        List<HoldingResponse> responses =
                holdings.stream()
                        .map(this::mapToResponse)
                        .toList();


        BigDecimal totalInvested =
                holdings.stream()
                        .map(Holding::getInvestedAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal currentValue =
                calculateCurrentValue(
                        holdings
                );


        BigDecimal profitLoss =
                currentValue.subtract(
                        totalInvested
                );

        BigDecimal unrealizedProfitLoss =
                currentValue.subtract(totalInvested);

        BigDecimal realizedProfitLoss =
                tradeRepository
                        .findByUserIdOrderByExecutedAtDesc(userId)
                        .stream()
                        .map(Trade::getRealizedProfitLoss)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfitLoss =
                realizedProfitLoss.add(unrealizedProfitLoss);

        return new PortfolioResponse(
                userId,
                responses,
                totalInvested,
                currentValue,
                profitLoss,
                unrealizedProfitLoss,
                realizedProfitLoss,
                totalProfitLoss
        );
    }


    // ============================================================
    // PRICE UPDATE FROM KAFKA
    // ============================================================

    public void handlePriceUpdate(
            PriceUpdatedEvent event) {

        String symbol =
                event.getSymbol().toUpperCase();


        /*
         * Find every user who owns this stock.
         */
        List<Holding> holdings =
                holdingRepository.findBySymbol(symbol);


        /*
         * No one owns this stock.
         * Nothing to update.
         */
        if (holdings.isEmpty()) {
            return;
        }


        /*
         * A stock price changed.
         *
         * Every user holding this stock may now have
         * a different portfolio value.
         */
        for (Holding holding : holdings) {

            Long userId =
                    holding.getUserId();


            /*
             * Recalculate the COMPLETE portfolio.
             *
             * Do NOT calculate only the changed stock.
             *
             * Example:
             *
             * User owns:
             *
             * AAPL = 2100
             * TSLA = 1500
             * MSFT = 3000
             *
             * If AAPL changes, we need:
             *
             * 2100 + 1500 + 3000
             *
             * and not just 2100.
             */
            PortfolioResponse portfolio =
                    calculatePortfolio(userId);


            /*
             * Update Redis with the fresh portfolio.
             */
            try {

                cacheService.save(
                        userId,
                        portfolio
                );

                System.out.println(
                        "Updated portfolio cache for user "
                                + userId
                                + " after "
                                + symbol
                                + " price update."
                );

            } catch (Exception e) {

                /*
                 * Redis is a cache.
                 * Kafka processing should not fail only
                 * because Redis is unavailable.
                 */
                System.out.println(
                        "Could not update portfolio cache for user "
                                + userId
                                + ": "
                                + e.getMessage()
                );
            }
        }
    }


    // ============================================================
    // CURRENT VALUE
    // ============================================================

    private BigDecimal calculateCurrentValue(
            List<Holding> holdings) {

        BigDecimal currentValue =
                BigDecimal.ZERO;


        for (Holding holding : holdings) {

            BigDecimal currentPrice = null;

            try {

                currentPrice =
                        portfolioPriceCacheService.getPrice(
                                holding.getSymbol()
                        );

            } catch (Exception e) {

                System.out.println(
                        "Could not fetch price for "
                                + holding.getSymbol()
                );
            }


            /*
             * If the price isn't available in Redis,
             * don't add anything for that stock.
             */
            if (currentPrice == null) {
                continue;
            }


            BigDecimal holdingValue =
                    currentPrice.multiply(
                            BigDecimal.valueOf(
                                    holding.getQuantity()
                            )
                    );


            currentValue =
                    currentValue.add(
                            holdingValue
                    );
        }


        return currentValue;
    }


    // ============================================================
    // MAPPER
    // ============================================================

    private HoldingResponse mapToResponse(
            Holding holding) {

        return new HoldingResponse(
                holding.getId(),
                holding.getUserId(),
                holding.getSymbol(),
                holding.getQuantity(),
                holding.getAverageBuyPrice(),
                holding.getInvestedAmount()
        );
    }


    // ============================================================
    // CACHE INVALIDATION
    // ============================================================

    private void invalidatePortfolioCache(
            Long userId) {

        try {

            cacheService.delete(userId);

            System.out.println(
                    "Invalidated portfolio cache for user "
                            + userId
            );

        } catch (Exception e) {

            /*
             * Redis failure must not cause
             * the order processing to fail.
             */
            System.out.println(
                    "Could not invalidate Redis cache: "
                            + e.getMessage()
            );
        }
    }

    public HoldingResponse getHolding(Long userId, String symbol) {

        Holding holding = holdingRepository
                .findByUserIdAndSymbol(userId, symbol.toUpperCase())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Holding not found for " + symbol
                        )
                );

        return mapToResponse(holding);
    }

    private void saveTrade(OrderExecutedEvent event) {

        BigDecimal realizedProfitLoss = BigDecimal.ZERO;

        if ("SELL".equalsIgnoreCase(event.getOrderType())) {

            Holding holding = holdingRepository
                    .findByUserIdAndSymbol(
                            event.getUserId(),
                            event.getSymbol()
                    )
                    .orElse(null);

            // We need the average buy price BEFORE the sell.
            // Therefore, this implementation should NOT fetch
            // the holding here after processSell().
        }

        Trade trade = Trade.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .symbol(event.getSymbol().toUpperCase())
                .orderType(event.getOrderType())
                .quantity(event.getQuantity())
                .executionPrice(event.getExecutionPrice())
                .realizedProfitLoss(realizedProfitLoss)
                .executedAt(event.getExecutedAt())
                .build();

        tradeRepository.save(trade);
    }

    public List<TradeResponse> getTradeHistory(Long userId) {
        return tradeRepository
                .findByUserIdOrderByExecutedAtDesc(userId)
                .stream().map(this::mapToTradeResponse).toList();
    }

    public List<TradeResponse> getTradeHistory(Long userId,String symbol) {
      return tradeRepository
              .findByUserIdAndSymbolOrderByExecutedAtDesc(userId,symbol)
              .stream().map(this::mapToTradeResponse).toList();
    }



    private TradeResponse mapToTradeResponse(Trade trade){
        return  TradeResponse.builder()
                .id(trade.getId())
                .orderId(trade.getOrderId())
                .symbol(trade.getSymbol())
                .orderType(trade.getOrderType())
                .quantity(trade.getQuantity())
                .executionPrice(trade.getExecutionPrice())
                .realizedProfitLoss(
                        trade.getRealizedProfitLoss()
                )
                .executedAt(trade.getExecutedAt())
                .build();
    }

}

