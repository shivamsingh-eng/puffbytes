package com.puffbytes.puffbytes.authentication.controller;

import com.puffbytes.puffbytes.authentication.dto.*;
import com.puffbytes.puffbytes.authentication.service.AuthService;
import com.puffbytes.puffbytes.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Logged out successfully")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/sso/google")
    public AuthResponse googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        return authService.googleLogin(request.getIdToken());
    }

    @PostMapping("/sso/github")
    public AuthResponse githubLogin(@Valid @RequestBody GithubAuthRequest request) {
        return authService.githubLogin(request.getCode());
    }
}
