package org.stock_trading.portfolio_service.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class UserContext {

    public Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");

        if (userId == null) {
            throw new RuntimeException("X-User-Id header missing");
        }

        return Long.valueOf(userId);
    }
}
