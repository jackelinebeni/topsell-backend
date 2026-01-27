package com.topsell.backend.repository;
import com.topsell.backend.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
public interface QuoteRepository extends JpaRepository<Quote, Long> {}