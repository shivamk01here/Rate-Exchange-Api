package com.example.exchangerate.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateLimitExceptionHandler implements WebFilter {

    private final ObjectMapper objectMapper;
    private final RateLimitService rateLimitService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).then(Mono.defer(() -> {
            if (exchange.getResponse().getStatusCode() != null
                    && exchange.getResponse().getStatusCode().value() == 429
                    && !exchange.getResponse().isCommitted()) {

                RateLimitEntry entry = null;
                String clientIp = resolveClientIp(exchange);
                String path = exchange.getRequest().getURI().getPath();
                entry = rateLimitService.getEntry(clientIp, path);

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("error", "Too Many Requests");
                body.put("message", "Rate limit exceeded. Please try again later.");
                body.put("path", path);
                if (entry != null) {
                    body.put("retryAfter", entry.getWindowEnd().getEpochSecond());
                }

                try {
                    byte[] bytes = objectMapper.writeValueAsBytes(body);
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                    return exchange.getResponse().writeWith(Mono.just(buffer));
                } catch (Exception e) {
                    return Mono.empty();
                }
            }
            return Mono.empty();
        }));
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
