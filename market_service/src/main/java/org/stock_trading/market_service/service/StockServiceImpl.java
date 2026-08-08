package org.stock_trading.market_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.stock_trading.market_service.config.StockCacheService;
import org.stock_trading.market_service.dto.CreateStockRequest;
import org.stock_trading.market_service.dto.StockResponse;
import org.stock_trading.market_service.dto.UpdatePriceRequest;
import org.stock_trading.market_service.entity.Stock;
import org.stock_trading.market_service.event.PriceUpdatedEvent;
import org.stock_trading.market_service.exception.DuplicateStockException;
import org.stock_trading.market_service.exception.StockNotFoundException;
import org.stock_trading.market_service.kafka.KafkaProducer;
import org.stock_trading.market_service.repository.StockRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private final StockRepository repository;

    @Autowired
    private final StockCacheService cacheService;

    @Autowired
    private final KafkaProducer kafkaProducer;

    public StockServiceImpl(StockRepository repository, StockCacheService cacheService, KafkaProducer kafkaProducer) {
        this.repository = repository;
        this.cacheService = cacheService;
        this.kafkaProducer = kafkaProducer;
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

        try{
            StockResponse cached = cacheService.get(cacheKey);
            if (cached != null) {
                System.out.println("Fetching from redis");
                return cached;
            }
        } catch (Exception e) {

        }


        Stock stock = repository.findBySymbol(symbol).orElseThrow(() -> new StockNotFoundException("Stock not found"));

        StockResponse response = mapToResponse(stock);
        // 3. Try saving result to Redis
        try {

            cacheService.save(cacheKey, response);

            System.out.println("Saving in Redis");

        } catch (Exception e) {

            System.out.println(
                    "Redis unavailable. Skipping cache."
            );
        }
        System.out.println("Saving in redis");
        return mapToResponse(stock);
    }

    @Override
    public StockResponse updatePrice(String symbol, UpdatePriceRequest request) {

        String cacheKey = "stock:" + symbol.toUpperCase();

        Stock stock = repository.findBySymbol(symbol).orElseThrow(() -> new StockNotFoundException("Stock not found"));

        BigDecimal oldPrice = stock.getCurrentPrice();
        stock.setCurrentPrice(request.getCurrentPrice());

        Stock updated = repository.save(stock);
        StockResponse response = mapToResponse(stock);
        cacheService.save(cacheKey,response);

        PriceUpdatedEvent event = new PriceUpdatedEvent(
                updated.getSymbol(),
                oldPrice,
                updated.getCurrentPrice(),
                updated.getUpdatedAt()
        );

        kafkaProducer.publishPriceUpdated(event);
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
