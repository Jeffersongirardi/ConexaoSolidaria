package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlugAndPublicadoTrue(String slug);
    Page<BlogPost> findByPublicadoTrueOrderByDataPublicacaoDesc(Pageable pageable);
    Page<BlogPost> findByPublicadoTrueAndCategoriaOrderByDataPublicacaoDesc(String categoria, Pageable pageable);
    List<BlogPost> findAllByOrderByDataCriacaoDesc();

    @Query("SELECT DISTINCT b.categoria FROM BlogPost b WHERE b.publicado = true")
    List<String> findCategoriasPublicadas();
}
