package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.BlogPost;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record BlogDTO(
        Long id,
        String titulo,
        String slug,
        String conteudo,
        String resumo,
        String categoria,
        String imagemUrl,
        Boolean publicado,
        LocalDateTime dataPublicacao,
        String autorNome) {

    public static BlogDTO from(BlogPost p) {
        return new BlogDTO(p.getId(), p.getTitulo(), p.getSlug(), p.getConteudo(), p.getResumo(),
                p.getCategoria(), p.getImagemUrl(), p.getPublicado(), p.getDataPublicacao(),
                p.getAutor() != null ? p.getAutor().getNome() : null);
    }

    public record BlogRequest(
            @NotBlank String titulo,
            @NotBlank String conteudo,
            String resumo,
            String categoria,
            String imagemUrl,
            Boolean publicado) {
    }
}
