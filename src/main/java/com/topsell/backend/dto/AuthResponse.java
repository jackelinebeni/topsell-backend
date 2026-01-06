package com.topsell.backend.dto;

import com.topsell.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private User user; // Devuelve el usuario para que el frontend sepa nombre/rol
}