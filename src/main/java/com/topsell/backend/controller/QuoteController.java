package com.topsell.backend.controller;

import com.topsell.backend.dto.QuoteRequest;
import com.topsell.backend.entity.Quote;
import com.topsell.backend.repository.QuoteRepository;
import com.topsell.backend.service.QuoteService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;
    private final QuoteRepository quoteRepository;

    @PostMapping("/send")
    public ResponseEntity<String> sendQuote(@RequestBody QuoteRequest request) {
        try {
            quoteService.sendQuote(request);
            return ResponseEntity.ok("Cotización enviada con éxito");
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body("Error al enviar el correo");
        }
    }

    @GetMapping
    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

}