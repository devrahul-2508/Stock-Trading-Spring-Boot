package org.stock_trading.market_service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
