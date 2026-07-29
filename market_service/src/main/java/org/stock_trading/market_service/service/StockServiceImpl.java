package org.stock_trading.market_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.stock_trading.market_service.config.StockCacheService;
import org.stock_trading.market_service.dto.CreateStockRequest;
import org.stock_trading.market_service.dto.StockResponse;
import org.stock_trading.market_service.dto.UpdatePriceRequest;
import org.stock_trading.market_service.entity.Stock;
import org.stock_trading.market_service.exception.DuplicateStockException;
import org.stock_trading.market_service.exception.StockNotFoundException;
import org.stock_trading.market_service.repository.StockRepository;

import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private final StockRepository repository;

    @Autowired
    private final StockCacheService cacheService;

    public StockServiceImpl(StockRepository repository, StockCacheService cacheService) {
        this.repository = repository;
        this.cacheService = cacheService;
    }

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

        String cacheKey = "stock:" + symbol.toUpperCase();

        StockResponse cached = cacheService.get(cacheKey);
        if (cached != null) {
            System.out.println("Fetching from redis");
            return cached;
        }

        Stock stock = repository.findBySymbol(symbol).orElseThrow(() -> new StockNotFoundException("Stock not found"));

        StockResponse response = mapToResponse(stock);
        cacheService.save(cacheKey,response);
        System.out.println("Saving in redis");
        return mapToResponse(stock);
    }

    @Override
    public StockResponse updatePrice(String symbol, UpdatePriceRequest request) {

        String cacheKey = "stock:" + symbol.toUpperCase();

        Stock stock = repository.findBySymbol(symbol).orElseThrow(() -> new StockNotFoundException("Stock not found"));

        stock.setCurrentPrice(request.getCurrentPrice());

        repository.save(stock);
        StockResponse response = mapToResponse(stock);
        cacheService.save(cacheKey,response);
        return response;
    }

    @Override
    public void deleteStock(String symbol) {
        Stock stock = repository.findBySymbol(symbol).orElseThrow(() -> new StockNotFoundException("Stock not found"));

        repository.delete(stock);
        cacheService.delete("stock:" + symbol.toUpperCase());

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
