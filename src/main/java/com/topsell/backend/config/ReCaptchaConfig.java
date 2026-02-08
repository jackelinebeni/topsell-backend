package com.topsell.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "recaptcha")
public class ReCaptchaConfig {
    private String secretKey;
    private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";
    private double minScore = 0.5; // Puntuación mínima para considerar válido (0.0 - 1.0)
}
