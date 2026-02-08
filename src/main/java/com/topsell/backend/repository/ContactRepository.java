package com.topsell.backend.repository;

import com.topsell.backend.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    // Ordenar por fecha de creación descendente (más recientes primero)
    List<Contact> findAllByOrderByFechaCreacionDesc();
    
    // Filtrar por leído/no leído
    List<Contact> findByLeidoOrderByFechaCreacionDesc(boolean leido);
}
