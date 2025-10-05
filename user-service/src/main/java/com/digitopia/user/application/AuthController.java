package com.digitopia.user.application;

import com.digitopia.common.dto.request.CreateUserRequest;
import com.digitopia.user.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<Map<String, Object>> register(
        @Valid @RequestBody CreateUserRequest request,
        @RequestParam String password
    ) {
        return ResponseEntity.ok(authService.register(request, password));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<Map<String, Object>> login(
        @RequestParam String email,
        @RequestParam String password
    ) {
        return ResponseEntity.ok(authService.login(email, password));
    }
}
