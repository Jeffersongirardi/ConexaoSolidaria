package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByUuid(String uuid);
    List<Payment> findByDoadorIdOrderByDataCriacaoDesc(Long doadorId);
    List<Payment> findByInstituicaoIdOrderByDataCriacaoDesc(Long instituicaoId);

    @Query("SELECT p.campaign.id, p.doador.id FROM Payment p WHERE p.campaign IS NOT NULL AND p.campaign.id IN :ids AND p.status <> 'cancelado'")
    List<Object[]> findDoadorIdsByCampaignIds(@Param("ids") List<Long> ids);

    @Query("SELECT p.campaign.id, SUM(p.valor) FROM Payment p WHERE p.campaign IS NOT NULL AND p.campaign.id IN :ids AND p.status = 'recebido' GROUP BY p.campaign.id")
    List<Object[]> sumValorRecebidoPorCampanha(@Param("ids") List<Long> ids);
}
