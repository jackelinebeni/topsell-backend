package com.topsell.backend.service;

import com.topsell.backend.config.JwtService;
import com.topsell.backend.dto.AuthResponse;
import com.topsell.backend.dto.LoginRequest;
import com.topsell.backend.dto.RegisterRequest;
import com.topsell.backend.dto.RegisterGuestRequest;
import com.topsell.backend.entity.Role;
import com.topsell.backend.entity.User;
import com.topsell.backend.exception.UnauthorizedAccessException;
import com.topsell.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // 1. REGISTRO NORMAL (CLIENTE)
    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getNombres())
                .lastName(request.getApellidos())
                .email(request.getEmail())
                .phone(request.getTelefono())
                .password(passwordEncoder.encode(request.getPassword())) // Encriptar password
                .role(Role.USER)
                .build();

        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).user(user).build();
    }

    // 2. REGISTRO INVITADO (SIN PASSWORD)
    public AuthResponse registerGuest(RegisterGuestRequest request) {
        // Verificamos si el email ya existe, si existe podríamos retornarlo o lanzar error.
        // Aquí asumimos creación nueva o actualización de datos básicos.

        var user = User.builder()
                .firstName(request.getNombres())
                .lastName(request.getApellidos())
                .email(request.getEmail())
                .phone(request.getTelefono())
                .password(null) // SIN PASSWORD
                .role(Role.GUEST)
                .build();

        repository.save(user);
        var jwtToken = jwtService.generateToken(user); // Generamos token igual para que pueda operar
        return AuthResponse.builder().token(jwtToken).user(user).build();
    }

    // 3. LOGIN
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).user(user).build();
    }

    // 4. LOGIN ADMIN
    public AuthResponse loginAdmin(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        
        // Validar que el usuario sea ADMIN
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Acceso denegado: Solo administradores pueden acceder a esta plataforma");
        }
        
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).user(user).build();
    }
}