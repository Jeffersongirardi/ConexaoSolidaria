package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.EmailLembrete;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLembreteRepository extends JpaRepository<EmailLembrete, Long> {
    boolean existsByTipoAndReferenciaIdAndUserId(String tipo, Long referenciaId, Long userId);
}
