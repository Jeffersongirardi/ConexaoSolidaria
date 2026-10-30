package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.AuthEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthEventRepository extends JpaRepository<AuthEvent, Long> {
    long countByEvento(String evento);
}
