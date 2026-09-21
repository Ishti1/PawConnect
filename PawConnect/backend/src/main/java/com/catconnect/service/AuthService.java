package com.catconnect.service;

import com.catconnect.dto.AuthRequest;
import com.catconnect.dto.AuthResponse;
import com.catconnect.dto.UserDto;
import com.catconnect.entity.User;
import com.catconnect.repository.UserRepository;
import com.catconnect.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${google.client.id:your_default_client_id_here}")
    private String googleClientId;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : "Cat Lover")
                .city(request.getCity() != null ? request.getCity() : "Cairo")
                .build();
        user = userRepository.save(user);
        return buildResponse(user);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return buildResponse(user);
    }

    public AuthResponse loginWithGoogle(String idTokenString) {
        log.info("[Google Login] Received idToken of length: {}", idTokenString != null ? idTokenString.length() : 0);
        log.info("[Google Login] Using googleClientId: {}", googleClientId);
        try {
            com.google.api.client.http.HttpTransport transport = new com.google.api.client.http.javanet.NetHttpTransport();
            com.google.api.client.json.JsonFactory jsonFactory = new com.google.api.client.json.gson.GsonFactory();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier = new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();

            log.info("[Google Login] Calling verifier.verify()...");
            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                log.info("[Google Login] Token verified successfully for email: {}", email);

                boolean isNewUser = false;
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    isNewUser = true;
                    user = User.builder()
                            .email(email)
                            .passwordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                            .displayName(name != null ? name : "Cat Lover")
                            .avatarUrl(pictureUrl)
                            .authProvider("GOOGLE")
                            .city("Cairo")
                            .build();
                    user = userRepository.save(user);
                }
                return buildResponse(user, isNewUser);
            } else {
                log.warn("[Google Login] verifier.verify() returned null - token is invalid or audience mismatch");
                throw new IllegalArgumentException("Invalid ID token.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Throwable e) {
            log.error("[Google Login] Exception during verification", e);
            throw new IllegalArgumentException("Google login failed: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public UserDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserDto.from(user);
    }

    private AuthResponse buildResponse(User user) {
        return buildResponse(user, false);
    }

    private AuthResponse buildResponse(User user, boolean isNewUser) {
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        UserDto userDto = UserDto.from(user);
        userDto.setIsNewUser(isNewUser);
        return AuthResponse.builder()
                .token(token)
                .user(userDto)
                .isAdmin(Boolean.TRUE.equals(user.getIsAdmin()))
                .build();
    }
    public AuthResponse updateAccount(com.catconnect.dto.UpdateAccountRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (request.getNewDisplayName() != null && !request.getNewDisplayName().isBlank()) {
            user.setDisplayName(request.getNewDisplayName().trim());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        user = userRepository.save(user);
        return buildResponse(user);
    }

    public UserDto updateDisplayName(Long userId, String newDisplayName) {
        if (newDisplayName == null || newDisplayName.isBlank()) {
            throw new IllegalArgumentException("Display name cannot be blank.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setDisplayName(newDisplayName.trim());
        user = userRepository.save(user);
        return UserDto.from(user);
    }
}

