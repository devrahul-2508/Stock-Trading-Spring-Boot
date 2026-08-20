package org.stock_trading.portfolio_service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "holdings",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "symbol"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal averageBuyPrice;

    @Column(nullable = false)
    private BigDecimal investedAmount;
}
