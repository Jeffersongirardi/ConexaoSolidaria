package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByInstitutionIdOrderByDataCriacaoDesc(Long instituicaoId);

    Page<Campaign> findByAtivoTrueOrderByDataCriacaoDesc(Pageable pageable);

    @Query("SELECT c FROM Campaign c WHERE c.ativo = true " +
           "AND (:categoria IS NULL OR c.categoria = :categoria) " +
           "AND (:urgencia IS NULL OR c.urgencia = :urgencia) " +
           "AND (:busca IS NULL OR LOWER(c.titulo) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           "OR LOWER(c.descricao) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
           "ORDER BY c.dataCriacao DESC")
    Page<Campaign> filtrar(@Param("categoria") String categoria,
                           @Param("urgencia") String urgencia,
                           @Param("busca") String busca,
                           Pageable pageable);

    List<Campaign> findTop6ByAtivoTrueOrderByDataCriacaoDesc();
    long countByAtivoTrue();
}
