package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Oferta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

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
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Oferta o WHERE o.id = :id")
    Optional<Oferta> findByIdForUpdate(@Param("id") Long id);
}
