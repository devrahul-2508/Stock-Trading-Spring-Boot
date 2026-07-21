package org.stock_trading.market_service.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.stock_trading.market_service.dto.CreateStockRequest;
import org.stock_trading.market_service.dto.StockResponse;
import org.stock_trading.market_service.dto.UpdatePriceRequest;
import org.stock_trading.market_service.service.StockService;

import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    @PostMapping
    public ResponseEntity<StockResponse> createStock(
            @Valid @RequestBody CreateStockRequest request) {

        StockResponse response = stockService.createStock(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStocks() {

        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> getStockBySymbol(
            @PathVariable String symbol) {

        return ResponseEntity.ok(stockService.getStockBySymbol(symbol));
    }

    @PutMapping("/{symbol}/price")
    public ResponseEntity<StockResponse> updatePrice(
            @PathVariable String symbol,
            @Valid @RequestBody UpdatePriceRequest request) {

        return ResponseEntity.ok(
                stockService.updatePrice(symbol, request)
        );
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> deleteStock(
            @PathVariable String symbol) {

        stockService.deleteStock(symbol);

        return ResponseEntity.noContent().build();
    }

}
