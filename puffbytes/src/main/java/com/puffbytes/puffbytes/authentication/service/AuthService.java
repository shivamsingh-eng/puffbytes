package com.puffbytes.puffbytes.authentication.service;

import com.puffbytes.puffbytes.authentication.dto.AuthResponse;
import com.puffbytes.puffbytes.authentication.dto.LoginRequest;
import com.puffbytes.puffbytes.authentication.dto.RegisterRequest;
import com.puffbytes.puffbytes.authentication.entity.RefreshToken;
import com.puffbytes.puffbytes.authentication.entity.User;
import com.puffbytes.puffbytes.authentication.enums.Provider;
import com.puffbytes.puffbytes.authentication.exception.EmailAlreadyExistException;
import com.puffbytes.puffbytes.authentication.exception.InvalidCredentialsException;
import com.puffbytes.puffbytes.authentication.exception.InvalidOrExpiredTokenException;
import com.puffbytes.puffbytes.authentication.exception.UserNotFoundException;
import com.puffbytes.puffbytes.authentication.repository.RefreshTokenRepository;
import com.puffbytes.puffbytes.authentication.repository.UserRepository;
import com.puffbytes.puffbytes.authentication.util.GoogleTokenVerifier;
import com.puffbytes.puffbytes.authentication.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.client.secret}")
    private String githubClientSecret;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RestTemplate restTemplate;

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistException("Email already exists");
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

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
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

        String newAccessToken = jwtUtil.generateToken(token.getUser().getEmail());

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

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse githubLogin(String code) {

        // 1. Exchange code get access token
        String tokenUrl = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> params = new HashMap<>();
        params.put("client_id", githubClientId);
        params.put("client_secret", githubClientSecret);
        params.put("code", code);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, request, Map.class);

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        if (accessToken == null) {
            throw new InvalidOrExpiredTokenException("Failed to get GitHub access token");
        }

        // 2. Get basic user info
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                userRequest,
                Map.class
        );

        Map<String, Object> userData = userResponse.getBody();

        String githubId = String.valueOf(userData.get("id"));
        String username = (String) userData.get("login");

        // 3. Fetch emails
        ResponseEntity<List> emailResponse = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                userRequest,
                List.class
        );

        List<Map<String, Object>> emails = emailResponse.getBody();

        String email = null;

        if (emails != null) {
            for (Map<String, Object> e : emails) {
                Boolean primary = (Boolean) e.get("primary");
                Boolean verified = (Boolean) e.get("verified");

                if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                    email = (String) e.get("email");
                    break;
                }
            }
        }

        // 4. Fallback if email not found
        if (email == null) {
            // fallback: use providerId-based unique email
            email = "github_" + githubId + "@puffbytes.com";
        }

        // 5. Find or create user
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

        // 6. Generate JWT + Refresh Token
        String jwt = jwtUtil.generateToken(user.getEmail());
        String refreshToken = UUID.randomUUID().toString();

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(refreshToken)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build()
        );

        // 7. Return response
        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();
    }
}