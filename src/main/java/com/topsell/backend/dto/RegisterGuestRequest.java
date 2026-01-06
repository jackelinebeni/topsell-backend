package com.topsell.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterGuestRequest {
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
}