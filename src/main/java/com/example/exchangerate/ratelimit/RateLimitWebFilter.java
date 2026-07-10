package com.example.exchangerate.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RateLimitWebFilter implements WebFilter {

    private final RateLimitService rateLimitService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientIp = getClientKey(exchange);
        String path = exchange.getRequest().getURI().getPath();

        if (!rateLimitService.isAllowed(clientIp, path)) {
            RateLimitEntry entry = rateLimitService.getEntry(clientIp, path);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            addRateLimitHeaders(exchange, entry);
            return exchange.getResponse().setComplete();
        }

        RateLimitEntry entry = rateLimitService.getEntry(clientIp, path);
        if (entry != null) {
            addRateLimitHeaders(exchange, entry);
        }

        return chain.filter(exchange);
    }

    private void addRateLimitHeaders(ServerWebExchange exchange, RateLimitEntry entry) {
        if (entry == null) return;
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(entry.getMaxRequests()));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(entry.getRemainingRequests()));
        exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(entry.getWindowEnd().getEpochSecond()));
    }

    private String getClientKey(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
