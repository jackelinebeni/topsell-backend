package com.topsell.backend.controller;

import com.topsell.backend.service.AuthenticationService;
import com.topsell.backend.dto.AuthResponse;
import com.topsell.backend.dto.LoginRequest;
import com.topsell.backend.dto.RegisterRequest;
import com.topsell.backend.dto.RegisterGuestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/register-guest")
    public ResponseEntity<AuthResponse> registerGuest(@RequestBody RegisterGuestRequest request) {
        return ResponseEntity.ok(service.registerGuest(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/login-admin")
    public ResponseEntity<AuthResponse> loginAdmin(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.loginAdmin(request));
    }
}