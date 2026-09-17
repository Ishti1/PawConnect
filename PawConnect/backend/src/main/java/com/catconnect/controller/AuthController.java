package com.catconnect.controller;

import com.catconnect.dto.AuthRequest;
import com.catconnect.dto.AuthResponse;
import com.catconnect.dto.UserDto;
import com.catconnect.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/me")
    public UserDto me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return authService.getProfile(userId);
    }
    @PutMapping("/manage")
    public AuthResponse manage(@Valid @RequestBody com.catconnect.dto.UpdateAccountRequest request) {
        return authService.updateAccount(request);
    }
}
