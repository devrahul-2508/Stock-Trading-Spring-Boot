package org.stock_trading.portfolio_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String symbol;

    @Column(name = "order_type", nullable = false)
    private String orderType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "execution_price", nullable = false)
    private BigDecimal executionPrice;

    @Column(name = "realized_profit_loss", nullable = false)
    private BigDecimal realizedProfitLoss;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;
}