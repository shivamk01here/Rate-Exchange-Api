package com.example.exchangerate.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitWebFilterTest {

    private RateLimitConfig config;
    private RateLimitService service;
    private RateLimitWebFilter filter;

    @Mock
    private WebFilterChain chain;

    @BeforeEach
    void setUp() {
        config = new RateLimitConfig();
        config.setEnabled(true);
        config.setDefaultRequestsPerWindow(3);
        config.setWindowSize(Duration.ofMinutes(1));
        config.setBypassPaths(Arrays.asList("/api/health"));
        service = new RateLimitService(config);
        filter = new RateLimitWebFilter(service);
    }

    @Test
    void filter_withinLimit_continuesChain() {
        ServerWebExchange exchange = createExchange("/api/rates");
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void filter_exceedsLimit_returns429() {
        ServerWebExchange exchange = createExchange("/api/rates");
        for (int i = 0; i < 3; i++) {
            filter.filter(exchange, createChain()).subscribe();
        }

        ServerWebExchange blockedExchange = createExchange("/api/rates");
        when(chain.filter(blockedExchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(blockedExchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, blockedExchange.getResponse().getStatusCode());
    }

    @Test
    void filter_bypassPath_alwaysAllowed() {
        for (int i = 0; i < 10; i++) {
            ServerWebExchange exchange = createExchange("/api/health");
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            StepVerifier.create(filter.filter(exchange, chain))
                    .verifyComplete();
        }
    }

    @Test
    void filter_addsRateLimitHeaders() {
        ServerWebExchange exchange = createExchange("/api/rates");
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).subscribe();

        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertNotNull(headers.get("X-RateLimit-Limit"));
        assertNotNull(headers.get("X-RateLimit-Remaining"));
        assertNotNull(headers.get("X-RateLimit-Reset"));
    }

    @Test
    void filter_clientIpFromXForwardedFor() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost/api/rates")
                .header("X-Forwarded-For", "10.0.0.1, 10.0.0.2")
                .remoteAddress(new InetSocketAddress("192.168.1.1", 8080))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).subscribe();

        RateLimitEntry entry = service.getEntry("10.0.0.1", "/api/rates");
        assertNotNull(entry);
    }

    private ServerWebExchange createExchange(String path) {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost" + path)
                .remoteAddress(new InetSocketAddress("192.168.1.1", 8080))
                .build();
        return MockServerWebExchange.from(request);
    }

    private WebFilterChain createChain() {
        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());
        return mockChain;
    }
}
