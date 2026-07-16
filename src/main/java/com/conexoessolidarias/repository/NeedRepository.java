package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Need;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NeedRepository extends JpaRepository<Need, Long> {
    List<Need> findByInstitutionIdOrderByDataCriacaoDesc(Long instituicaoId);

    Page<Need> findByAtivoTrueOrderByDataCriacaoDesc(Pageable pageable);

    @Query("SELECT n FROM Need n WHERE n.ativo = true " +
           "AND (:categoria IS NULL OR n.categoria = :categoria) " +
           "AND (:urgencia IS NULL OR n.urgencia = :urgencia) " +
           "AND (:busca IS NULL OR LOWER(n.titulo) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           "OR LOWER(n.descricao) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
           "ORDER BY n.dataCriacao DESC")
    Page<Need> filtrar(@Param("categoria") String categoria,
                       @Param("urgencia") String urgencia,
                       @Param("busca") String busca,
                       Pageable pageable);

    List<Need> findTop6ByAtivoTrueOrderByDataCriacaoDesc();
    long countByAtivoTrue();
}
