package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.Campaign;
import com.conexoessolidarias.service.CampaignStatsService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        List<ImageDTO> imagens,
        Integer numDoadores,
        BigDecimal valorRecebido,
        Integer numDoacoesItens,
        Map<String, Integer> itensPorCategoria,
        String instrucoesEntrega,
        String enderecoEntrega) {

    public record InstitutionSummary(Long id, String razaoSocial, String nomeFantasia,
                                     String fotoUrl, String cidade) {
    }

    public record ImageDTO(Long id, String url, String legenda, Integer ordem) {
    }

    public static CampaignDTO from(Campaign c) {
        return from(c, CampaignStatsService.CampaignMetric.vazio());
    }

    public static CampaignDTO from(Campaign c, int numDoadores) {
        return from(c, new CampaignStatsService.CampaignMetric(numDoadores,
                BigDecimal.ZERO, 0, Map.of()));
    }

    public static CampaignDTO from(Campaign c, CampaignStatsService.CampaignMetric m) {
        var inst = c.getInstitution();
        var summary = inst != null ? new InstitutionSummary(inst.getId(), inst.getRazaoSocial(),
                inst.getNomeFantasia(), inst.getFotoUrl(),
                inst.getUser() != null ? inst.getUser().getCidade() : null) : null;
        List<ImageDTO> imgs = c.getImages() != null
                ? c.getImages().stream()
                    .map(i -> new ImageDTO(i.getId(), i.getFilename(), i.getLegenda(), i.getOrdem()))
                    .toList()
                : List.of();
        String endEntrega = c.getEnderecoEntrega() != null && !c.getEnderecoEntrega().isBlank()
                ? c.getEnderecoEntrega()
                : (c.getInstitution() != null ? c.getInstitution().getEndereco() : null);
        return new CampaignDTO(c.getId(), c.getTitulo(), c.getDescricao(), c.getCategoria(),
                c.getQuantidadeAlvo(), c.getUrgencia(), c.getAceitaFinanceiro(),
                c.getProgresso(), c.getAtivo(), c.getDataCriacao(), summary, imgs,
                m.numDoadores(), m.valorRecebido(), m.numDoacoesItens(), m.itensPorCategoria(),
                c.getInstrucoesEntrega(), endEntrega);
    }
}
