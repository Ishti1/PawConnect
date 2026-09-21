package com.catconnect.controller;

import com.catconnect.dto.AuthRequest;
import com.catconnect.dto.AuthResponse;
import com.catconnect.dto.GoogleAuthRequest;
import com.catconnect.dto.UserDto;
import com.catconnect.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse loginWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        return authService.loginWithGoogle(request.getIdToken());
    }

    @GetMapping("/me")
    public UserDto me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return authService.getProfile(userId);
    }
    @PutMapping("/manage")
    public AuthResponse manage(@Valid @RequestBody com.catconnect.dto.UpdateAccountRequest request) {
        return authService.updateAccount(request);
    }

    @PatchMapping("/display-name")
    public UserDto updateDisplayName(@RequestBody Map<String, String> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String newDisplayName = body.get("displayName");
        return authService.updateDisplayName(userId, newDisplayName);
    }
}
