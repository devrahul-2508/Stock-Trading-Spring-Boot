package org.stock_trading.portfolio_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.stock_trading.portfolio_service.dto.HoldingResponse;
import org.stock_trading.portfolio_service.dto.PortfolioResponse;
import org.stock_trading.portfolio_service.dto.TradeResponse;
import org.stock_trading.portfolio_service.entity.Holding;
import org.stock_trading.portfolio_service.repository.HoldingRepository;
import org.stock_trading.portfolio_service.service.PortfolioService;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioServiceController {

    private final HoldingRepository holdingRepository;
    private final PortfolioService portfolioService;

    @GetMapping
    public PortfolioResponse getPortfolio() {

        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return portfolioService.getPortfolio(userId);
    }

    @GetMapping("/{symbol}")
    public HoldingResponse getHolding(
            @PathVariable String symbol
    ){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return portfolioService.getHolding(userId, symbol);
    }

    @GetMapping("/trades")
    public ResponseEntity<List<TradeResponse>> getTradeHistory(
            Authentication authentication,
            @RequestParam(required = false) String symbol
    ) {

        Long userId = (Long) authentication.getPrincipal();

        if (symbol == null || symbol.isBlank()) {
            return ResponseEntity.ok(
                    portfolioService.getTradeHistory(userId)
            );
        }

        return ResponseEntity.ok(
                portfolioService.getTradeHistory(userId, symbol)
        );
    }
}
