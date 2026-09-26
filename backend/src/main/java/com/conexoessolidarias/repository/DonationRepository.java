package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Donation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    @EntityGraph(attributePaths = {"updates"})
    List<Donation> findByDoadorIdOrderByDataIntencaoDesc(Long doadorId);

    @EntityGraph(attributePaths = {"updates"})
    List<Donation> findByCampaignIdInOrderByDataIntencaoDesc(List<Long> campaignIds);

    long countByStatus(String status);
}
