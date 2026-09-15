package org.stock_trading.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.stock_trading.order_service.entity.Order;
import org.stock_trading.order_service.enums.OrderStatus;
import org.stock_trading.order_service.enums.OrderType;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByUserId(String userId);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            OrderStatus status
    );

    List<Order> findByUserIdAndOrderTypeOrderByCreatedAtDesc(
            Long userId,
            OrderType orderType
    );

    List<Order> findByUserIdAndSymbolOrderByCreatedAtDesc(
            Long userId,
            String symbol
    );


}
