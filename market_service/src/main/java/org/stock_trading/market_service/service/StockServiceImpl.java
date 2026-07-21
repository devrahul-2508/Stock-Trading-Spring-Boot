package org.stock_trading.market_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.stock_trading.market_service.dto.CreateStockRequest;
import org.stock_trading.market_service.dto.StockResponse;
import org.stock_trading.market_service.dto.UpdatePriceRequest;
import org.stock_trading.market_service.entity.Stock;
import org.stock_trading.market_service.exception.DuplicateStockException;
import org.stock_trading.market_service.exception.StockNotFoundException;
import org.stock_trading.market_service.repository.StockRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository repository;

    @Override
    public StockResponse createStock(CreateStockRequest request) {
        if (repository.existsBySymbol(request.getSymbol())) {
            throw new DuplicateStockException("Stock already exists");
        }

        Stock stock = Stock.builder()
                .symbol(request.getSymbol().toUpperCase())
                .companyName(request.getCompanyName())
                .currentPrice(request.getCurrentPrice())
                .exchange(request.getExchange())
                .sector(request.getSector())
                .build();

        Stock savedStock = repository.save(stock);
        return mapToResponse(savedStock);
    }

    @Override
    public List<StockResponse> getAllStocks() {

        return repository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = repository.findBySymbol(symbol).orElseThrow(() -> new StockNotFoundException("Stock not found"));

        return mapToResponse(stock);
    }

    @Override
    public StockResponse updatePrice(String symbol, UpdatePriceRequest request) {
        Stock stock = repository.findBySymbol(symbol).orElseThrow(() -> new StockNotFoundException("Stock not found"));

        stock.setCurrentPrice(request.getCurrentPrice());

        repository.save(stock);
        return mapToResponse(stock);
    }

    @Override
    public void deleteStock(String symbol) {
        Stock stock = repository.findBySymbol(symbol).orElseThrow(() -> new StockNotFoundException("Stock not found"));

        repository.delete(stock);
    }

    private StockResponse mapToResponse(Stock stock) {

        return StockResponse.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .currentPrice(stock.getCurrentPrice())
                .exchange(stock.getExchange())
                .sector(stock.getSector())
                .build();
    }
}
