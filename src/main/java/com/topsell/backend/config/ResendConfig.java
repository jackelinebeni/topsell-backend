package com.topsell.backend.config;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResendConfig {

    @Value("${resend.api.key}")
    private String apiKey;

    @Bean
    public Resend resendClient() {
        // Esto crea la instancia que se inyectará en tu QuoteService
        return new Resend(apiKey);
    }
}