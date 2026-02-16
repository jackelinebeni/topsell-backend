package com.topsell.backend.repository;

import com.topsell.backend.entity.UserSuscriptores;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSuscripcionRepository extends JpaRepository<UserSuscriptores, Long> {
}
