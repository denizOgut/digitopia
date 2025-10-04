package com.digitopia.gateway.security.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();

            if (isPublicEndpoint(request.getPath().toString())) {
                return chain.filter(exchange);
            }

            var authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            var token = authHeader.substring(7);

            try {
                Claims claims = jwtTokenProvider.validateToken(token);

                if (jwtTokenProvider.isTokenExpired(claims)) {
                    return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
                }

                var userId = jwtTokenProvider.getUserId(claims);
                var role = jwtTokenProvider.getRole(claims);
                var email = jwtTokenProvider.getEmail(claims);

                var modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .header("X-User-Email", email)
                    .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicEndpoint(String path) {
        return path.contains("/auth/") ||
            path.contains("/healtz") ||
            path.contains("/actuator/") ||
            path.contains("/test/rate-limit") ||
            path.contains("/test/jwt");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        var errorResponse = """
            {
              "success": false,
              "message": "%s",
              "timestamp": "%s"
            }
            """.formatted(message, java.time.LocalDateTime.now());

        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    public static class Config {
    }
}
