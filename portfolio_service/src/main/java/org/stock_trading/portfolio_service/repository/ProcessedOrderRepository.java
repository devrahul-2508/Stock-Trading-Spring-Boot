package org.stock_trading.portfolio_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.stock_trading.portfolio_service.entity.ProcessedOrder;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder,Long> {


}
