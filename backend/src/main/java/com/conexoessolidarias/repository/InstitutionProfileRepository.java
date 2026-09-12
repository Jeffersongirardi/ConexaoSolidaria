package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.InstitutionProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InstitutionProfileRepository extends JpaRepository<InstitutionProfile, Long> {
    List<InstitutionProfile> findByAprovadoOrderByDataCadastroDesc(Boolean aprovado);
    List<InstitutionProfile> findAllByOrderByDataCadastroDesc();
    long countByAprovado(Boolean aprovado);
}
