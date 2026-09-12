package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByUuid(String uuid);
    List<Payment> findByDoadorIdOrderByDataCriacaoDesc(Long doadorId);
    List<Payment> findByInstituicaoIdOrderByDataCriacaoDesc(Long instituicaoId);
}
