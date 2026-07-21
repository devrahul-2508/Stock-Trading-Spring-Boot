package org.stock_trading.market_service.service;

import org.stock_trading.market_service.dto.CreateStockRequest;
import org.stock_trading.market_service.dto.StockResponse;
import org.stock_trading.market_service.dto.UpdatePriceRequest;

import java.util.List;

public interface StockService {

    StockResponse createStock(CreateStockRequest request);

    List<StockResponse> getAllStocks();

    StockResponse getStockBySymbol(String symbol);

    StockResponse updatePrice(
            String symbol,
            UpdatePriceRequest request);

    void deleteStock(String symbol);
}
