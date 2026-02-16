package com.topsell.backend.controller;

import com.topsell.backend.dto.QuoteRequest;
import com.topsell.backend.dto.QuoteResponseDto;
import com.topsell.backend.dto.QuoteItemResponseDto;
import com.topsell.backend.entity.Quote;
import com.topsell.backend.repository.QuoteRepository;
import com.topsell.backend.service.QuoteService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al enviar el correo");
        }
    }

    // ========== ENDPOINTS ADMIN ==========

    @GetMapping("/admin")
    public List<QuoteResponseDto> getAllQuotes() {
        return quoteRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<QuoteResponseDto> getQuoteById(@PathVariable Long id) {
        return quoteRepository.findById(id)
                .map(quote -> ResponseEntity.ok(convertToDto(quote)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteQuote(@PathVariable Long id) {
        return quoteRepository.findById(id)
                .map(quote -> {
                    quoteRepository.delete(quote);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Helper method para convertir Quote a QuoteResponseDto
    private QuoteResponseDto convertToDto(Quote quote) {
        QuoteResponseDto dto = new QuoteResponseDto();
        dto.setId(quote.getId());
        dto.setDate(quote.getDate());
        dto.setTotalAmount(quote.getTotalAmount());
        dto.setUserId(quote.getUser().getId());
        dto.setUserEmail(quote.getUser().getEmail());
        dto.setUserName(quote.getUser().getFirstName() + " " + quote.getUser().getLastName());
        
        List<QuoteItemResponseDto> itemDtos = quote.getItems().stream()
                .map(item -> {
                    QuoteItemResponseDto itemDto = new QuoteItemResponseDto();
                    itemDto.setId(item.getId());
                    itemDto.setProductId(item.getProduct().getId());
                    itemDto.setProductName(item.getProduct().getName());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setUnitPrice(item.getUnitPrice());
                    itemDto.setSubtotal(item.getSubtotal());
                    return itemDto;
                })
                .collect(Collectors.toList());
        
        dto.setItems(itemDtos);
        return dto;
    }

}