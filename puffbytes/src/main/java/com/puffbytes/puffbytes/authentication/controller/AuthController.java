package com.puffbytes.puffbytes.authentication.controller;

import com.puffbytes.puffbytes.authentication.dto.*;
import com.puffbytes.puffbytes.authentication.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {authService.register(request);
        return "User registered successfully";
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
    public String logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return "Logged out successfully";
    }

    @PostMapping("/sso/google")
    public AuthResponse googleLogin(@RequestBody GoogleAuthRequest request) {
        return authService.googleLogin(request.getIdToken());
    }

    @PostMapping("/sso/github")
    public AuthResponse githubLogin(@RequestBody GithubAuthRequest request) {
        return authService.githubLogin(request.getCode());
    }
}