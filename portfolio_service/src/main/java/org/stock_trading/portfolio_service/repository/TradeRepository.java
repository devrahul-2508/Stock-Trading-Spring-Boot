package org.stock_trading.portfolio_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.stock_trading.portfolio_service.entity.Trade;

import java.util.List;

public interface TradeRepository  extends JpaRepository<Trade,Long> {
    List<Trade> findByUserIdOrderByExecutedAtDesc(Long userId);

    List<Trade> findByUserIdAndSymbolOrderByExecutedAtDesc(
            Long userId,
            String symbol
    );
}
