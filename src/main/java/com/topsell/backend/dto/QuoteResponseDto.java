package com.topsell.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuoteResponseDto {
    private Long id;
    private LocalDateTime date;
    private BigDecimal totalAmount;
    private Long userId;
    private String userEmail;
    private String userName;
    private List<QuoteItemResponseDto> items;
}
