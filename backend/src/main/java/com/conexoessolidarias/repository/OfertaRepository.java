package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Oferta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {
    long countByStatus(String status);
    @EntityGraph(attributePaths = {"images"})
    List<Oferta> findByStatusOrderByDataCriacaoDesc(String status);
    @EntityGraph(attributePaths = {"images"})
    List<Oferta> findByDoadorIdOrderByDataCriacaoDesc(Long doadorId);
    @EntityGraph(attributePaths = {"images"})
    List<Oferta> findByInstituicaoIdAndStatusOrderByReservadaEmDesc(Long instituicaoId, String status);
    @EntityGraph(attributePaths = {"images"})
    List<Oferta> findAllByOrderByDataCriacaoDesc();
}
