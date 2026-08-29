package org.stock_trading.portfolio_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.stock_trading.portfolio_service.dto.PortfolioResponse;
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

    @GetMapping("/{userId}")
    public PortfolioResponse getPortfolio(
            @PathVariable Long userId) {

        return portfolioService.getPortfolio(userId);
    }
}
