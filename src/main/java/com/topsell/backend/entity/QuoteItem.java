package com.topsell.backend.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class QuoteItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;
    private BigDecimal unitPrice; // Precio al momento de cotizar
    private BigDecimal subtotal;

    @ManyToOne
    private Product product;
}