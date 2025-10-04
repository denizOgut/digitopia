package com.digitopia.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/jwt")
    public Mono<Map<String, Object>> testJwt(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-User-Role", required = false) String role,
        @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        return Mono.just(Map.of(
            "message", "JWT authentication successful",
            "userId", userId != null ? userId : "not found",
            "role", role != null ? role : "not found",
            "email", email != null ? email : "not found",
            "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/rate-limit")
    public Mono<Map<String, Object>> testRateLimit() {
        return Mono.just(Map.of(
            "message", "Rate limit test - request allowed",
            "timestamp", LocalDateTime.now()
        ));
    }
}
