package org.stock_trading.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.stock_trading.order_service.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByUserId(String userId);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
