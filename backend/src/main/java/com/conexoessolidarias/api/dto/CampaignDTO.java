package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.Campaign;
import java.time.LocalDateTime;
import java.util.List;

public record CampaignDTO(
        Long id,
        String titulo,
        String descricao,
        String categoria,
        String quantidadeAlvo,
        String urgencia,
        Boolean aceitaFinanceiro,
        Integer progresso,
        Boolean ativo,
        LocalDateTime dataCriacao,
        InstitutionSummary instituicao,
        List<ImageDTO> imagens) {

    public record InstitutionSummary(Long id, String razaoSocial, String nomeFantasia,
                                     String fotoUrl, String cidade) {
    }

    public record ImageDTO(Long id, String url, String legenda, Integer ordem) {
    }

    public static CampaignDTO from(Campaign c) {
        var inst = c.getInstitution();
        var summary = inst != null ? new InstitutionSummary(inst.getId(), inst.getRazaoSocial(),
                inst.getNomeFantasia(), inst.getFotoUrl(),
                inst.getUser() != null ? inst.getUser().getCidade() : null) : null;
        List<ImageDTO> imgs = c.getImages() != null
                ? c.getImages().stream()
                    .map(i -> new ImageDTO(i.getId(), i.getFilename(), i.getLegenda(), i.getOrdem()))
                    .toList()
                : List.of();
        return new CampaignDTO(c.getId(), c.getTitulo(), c.getDescricao(), c.getCategoria(),
                c.getQuantidadeAlvo(), c.getUrgencia(), c.getAceitaFinanceiro(),
                c.getProgresso(), c.getAtivo(), c.getDataCriacao(), summary, imgs);
    }
}
