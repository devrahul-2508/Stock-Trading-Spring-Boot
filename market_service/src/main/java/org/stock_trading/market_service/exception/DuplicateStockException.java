package org.stock_trading.market_service.exception;

public class DuplicateStockException  extends RuntimeException{

    public DuplicateStockException(String message){
        super(message);
    }
}
