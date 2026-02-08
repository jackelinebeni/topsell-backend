package com.topsell.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.topsell.backend.config.ReCaptchaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReCaptchaService {

    private final ReCaptchaConfig reCaptchaConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("reCAPTCHA token is empty");
            return false;
        }

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", reCaptchaConfig.getSecretKey());
            params.add("response", token);

            String response = restTemplate.postForObject(
                reCaptchaConfig.getVerifyUrl(),
                params,
                String.class
            );

            JsonNode responseJson = objectMapper.readTree(response);
            boolean success = responseJson.get("success").asBoolean();

            if (success) {
                double score = responseJson.get("score").asDouble();
                log.info("reCAPTCHA verification success. Score: {}", score);
                
                if (score < reCaptchaConfig.getMinScore()) {
                    log.warn("reCAPTCHA score {} is below minimum {}", score, reCaptchaConfig.getMinScore());
                    return false;
                }
                
                return true;
            } else {
                log.warn("reCAPTCHA verification failed: {}", response);
                return false;
            }

        } catch (Exception e) {
            log.error("Error verifying reCAPTCHA token", e);
            return false;
        }
    }
}
