package com.digitopia.gateway.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GlobalRateLimitWebFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GlobalRateLimitWebFilter.class);
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var path = exchange.getRequest().getPath().toString();

        if (path.contains("/healtz") || path.contains("/actuator")) {
            return chain.filter(exchange);
        }

        var key = getClientKey(exchange);
        var bucket = resolveBucket(key);

        var available = bucket.getAvailableTokens();
        log.info("[{}] Rate Limit Check - Available: {}", key, available);

        if (bucket.tryConsume(1)) {
            var remaining = bucket.getAvailableTokens();
            log.info("[{}] Request ALLOWED - Remaining: {}", key, remaining);

            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));

            return chain.filter(exchange);
        } else {
            log.warn("[{}] Rate Limit EXCEEDED", key);
            return handleRateLimitExceeded(exchange);
        }
    }

    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> {
            log.debug("Creating new bucket for key: {}", k);
            return createNewBucket();
        });
    }

    private Bucket createNewBucket() {
        var limit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    private String getClientKey(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            return "user:" + userId;
        }

        String ip = exchange.getRequest().getRemoteAddress() != null
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            : "unknown";

        return "ip:" + ip;
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After-Seconds", "10");

        String errorResponse = String.format("""
            {
              "success": false,
              "message": "Rate limit exceeded. Please try again later.",
              "timestamp": "%s"
            }
            """, LocalDateTime.now());

        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
