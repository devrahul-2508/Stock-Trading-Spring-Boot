package org.stock_trading.portfolio_service.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrder {

    @Id
    private Long orderId;
}