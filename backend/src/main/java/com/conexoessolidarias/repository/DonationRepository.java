package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Donation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    @EntityGraph(attributePaths = {"updates"})
    List<Donation> findByDoadorIdOrderByDataIntencaoDesc(Long doadorId);

    @EntityGraph(attributePaths = {"updates"})
    List<Donation> findByCampaignIdInOrderByDataIntencaoDesc(List<Long> campaignIds);

    @Query("SELECT d.campaign.id, d.doador.id FROM Donation d WHERE d.campaign.id IN :ids AND d.status <> 'cancelado'")
    List<Object[]> findDoadorIdsByCampaignIds(@Param("ids") List<Long> ids);

    @Query("SELECT d.campaign.id, d.categoria, COUNT(d) FROM Donation d WHERE d.campaign.id IN :ids AND d.status = 'recebido' GROUP BY d.campaign.id, d.categoria")
    List<Object[]> countItensRecebidosPorCategoria(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"updates"})
    List<Donation> findTop100ByOrderByDataIntencaoDesc();

    long countByStatus(String status);
}
