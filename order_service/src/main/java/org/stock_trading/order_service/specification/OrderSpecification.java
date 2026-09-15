package org.stock_trading.order_service.specification;

import org.springframework.data.jpa.domain.Specification;
import org.stock_trading.order_service.entity.Order;
import org.stock_trading.order_service.enums.OrderStatus;
import org.stock_trading.order_service.enums.OrderType;

public class OrderSpecification {
    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Order> hasOrderType(OrderType orderType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("orderType"), orderType);
    }

    public static Specification<Order> hasSymbol(String symbol) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("symbol"),
                        symbol.toUpperCase()
                );
    }
}
