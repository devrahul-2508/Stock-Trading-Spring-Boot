package org.stock_trading.portfolio_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.stock_trading.portfolio_service.dto.HoldingResponse;
import org.stock_trading.portfolio_service.dto.PortfolioResponse;
import org.stock_trading.portfolio_service.entity.Holding;
import org.stock_trading.portfolio_service.event.OrderExecutedEvent;
import org.stock_trading.portfolio_service.event.PriceUpdatedEvent;
import org.stock_trading.portfolio_service.repository.HoldingRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;

    private final PortfolioCacheService cacheService;

    private final PortfolioPriceCacheService portfolioPriceCacheService;


    // ============================================================
    // ORDER EXECUTION
    // ============================================================

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
                                )
                        );


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

            /*
             * Keep the same average buy price.
             *
             * Example:
             *
             * 10 shares
             * Average price = 200
             *
             * Sell 4
             *
             * Remaining invested amount:
             *
             * 6 × 200 = 1200
             */

            BigDecimal remainingInvested =
                    holding.getAverageBuyPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            remainingQuantity
                                    )
                            );


            holding.setQuantity(
                    remainingQuantity
            );

            holding.setInvestedAmount(
                    remainingInvested
            );


            holdingRepository.save(holding);
        }


        /*
         * Whether we deleted or updated the holding,
         * the cached portfolio is stale.
         */
        invalidatePortfolioCache(
                event.getUserId()
        );
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


        return new PortfolioResponse(
                userId,
                responses,
                totalInvested,
                currentValue,
                profitLoss
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
}

