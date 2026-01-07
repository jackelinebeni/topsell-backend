package com.topsell.backend.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Quote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;
    private BigDecimal totalAmount;

    @ManyToOne
    private User user; // Quién cotizó

    @OneToMany(cascade = CascadeType.ALL)
    private List<QuoteItem> items;
}