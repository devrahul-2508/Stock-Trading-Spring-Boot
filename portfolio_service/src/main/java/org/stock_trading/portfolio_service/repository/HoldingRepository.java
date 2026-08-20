package org.stock_trading.portfolio_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.stock_trading.portfolio_service.entity.Holding;

import java.util.List;
import java.util.Optional;

public interface  HoldingRepository extends JpaRepository<Holding,Long> {

    Optional<Holding> findByUserIdAndSymbol(
            Long userId,
            String symbol
    );

    List<Holding> findByUserId(Long userId);
}
