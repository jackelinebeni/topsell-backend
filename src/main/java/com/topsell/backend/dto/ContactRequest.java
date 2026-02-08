package com.topsell.backend.dto;

import lombok.Data;

@Data
public class ContactRequest {
    private String nombres;
    private String apellidos;
    private String dniOrRuc;
    private String razonSocial;
    private String correo;
    private String mensaje;
    private String recaptchaToken;
}
