package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDoadorIdOrderByDataIntencaoDesc(Long doadorId);
    List<Donation> findByNeedIdInOrderByDataIntencaoDesc(List<Long> needIds);
    long countByStatus(String status);
}
