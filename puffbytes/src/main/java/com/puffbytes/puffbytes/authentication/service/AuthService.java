package com.puffbytes.puffbytes.authentication.service;

import com.puffbytes.puffbytes.authentication.config.GithubClientProperties;
import com.puffbytes.puffbytes.authentication.config.JwtProperties;
import com.puffbytes.puffbytes.authentication.dto.AuthResponse;
import com.puffbytes.puffbytes.authentication.dto.LoginRequest;
import com.puffbytes.puffbytes.authentication.dto.RegisterRequest;
import com.puffbytes.puffbytes.authentication.entity.RefreshToken;
import com.puffbytes.puffbytes.authentication.entity.User;
import com.puffbytes.puffbytes.authentication.enums.Provider;
import com.puffbytes.puffbytes.common.exception.EmailAlreadyExistsException;
import com.puffbytes.puffbytes.common.exception.InvalidCredentialsException;
import com.puffbytes.puffbytes.common.exception.InvalidOrExpiredTokenException;
import com.puffbytes.puffbytes.authentication.repository.RefreshTokenRepository;
import com.puffbytes.puffbytes.authentication.repository.UserRepository;
import com.puffbytes.puffbytes.authentication.util.GoogleTokenVerifier;
import com.puffbytes.puffbytes.authentication.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GithubClientProperties githubClient;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RestTemplate restTemplate;

    private Duration refreshTtl() {
        return Duration.ofMillis(jwtProperties.getRefresh().getExpiration());
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(Provider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null
                || user.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateToken(user.getId());
        String refreshToken = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plus(refreshTtl()))
                .user(user)
                .build();

        refreshTokenRepository.save(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refresh(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidOrExpiredTokenException("Refresh token expired");
        }

        String newAccessToken = jwtUtil.generateToken(token.getUser().getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid token"));

        refreshTokenRepository.delete(token);
    }

    public AuthResponse googleLogin(String idToken) {

        var payload = googleTokenVerifier.verify(idToken);

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String googleId = payload.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {

                    User newUser = User.builder()
                            .email(email)
                            .username(name)
                            .provider(Provider.GOOGLE)
                            .providerId(googleId)
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return userRepository.save(newUser);
                });

        String accessToken = jwtUtil.generateToken(user.getId());
        String refreshToken = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plus(refreshTtl()))
                .build();

        refreshTokenRepository.save(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse githubLogin(String code) {

        String tokenUrl = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> params = new HashMap<>();
        params.put("client_id", githubClient.getId());
        params.put("client_secret", githubClient.getSecret());
        params.put("code", code);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> tokenBody = tokenResponse.getBody();
        if (tokenBody == null) {
            throw new InvalidOrExpiredTokenException("Failed to get GitHub access token");
        }
        Object at = tokenBody.get("access_token");
        String accessToken = at != null ? at.toString() : null;

        if (accessToken == null) {
            throw new InvalidOrExpiredTokenException("Failed to get GitHub access token");
        }

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                userRequest,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> userData = userResponse.getBody();
        if (userData == null) {
            throw new InvalidOrExpiredTokenException("Failed to read GitHub user");
        }

        String githubId = String.valueOf(userData.get("id"));
        String username = userData.get("login") != null ? userData.get("login").toString() : null;

        ResponseEntity<List<Map<String, Object>>> emailResponse = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                userRequest,
                new ParameterizedTypeReference<>() {}
        );

        List<Map<String, Object>> emails = emailResponse.getBody();

        String email = null;

        if (emails != null) {
            for (Map<String, Object> e : emails) {
                Boolean primary = (Boolean) e.get("primary");
                Boolean verified = (Boolean) e.get("verified");

                if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                    Object ev = e.get("email");
                    email = ev != null ? ev.toString() : null;
                    break;
                }
            }
        }

        if (email == null) {
            email = "github_" + githubId + "@puffbytes.com";
        }

        String finalEmail = email;
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(finalEmail)
                                .username(username)
                                .provider(Provider.GITHUB)
                                .providerId(githubId)
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));

        String jwt = jwtUtil.generateToken(user.getId());
        String refreshToken = UUID.randomUUID().toString();

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(refreshToken)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plus(refreshTtl()))
                        .build()
        );

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();
    }
}
