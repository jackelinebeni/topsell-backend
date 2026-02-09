package com.topsell.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuoteItemResponseDto {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
