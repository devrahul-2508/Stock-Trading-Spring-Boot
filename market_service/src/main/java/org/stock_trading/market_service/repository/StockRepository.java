package org.stock_trading.market_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.stock_trading.market_service.entity.Stock;

import java.util.Optional;


public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);

    void deleteBySymbol(String symbol);
}
