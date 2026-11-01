package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.service.CampaignStatsService;
import java.util.List;
import java.util.Map;

public record InstitutionDTO(
        Long id,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String endereco,
        String website,
        String fotoUrl,
        String descricao,
        String categoriaAtuacao,
        String whatsapp,
        String pixKey,
        String pixTitular,
        Boolean aprovado,
        String motivoRecusa,
        String email,
        String telefone,
        String cidade,
        String estado,
        List<CampaignDTO> campanhas) {

    public static InstitutionDTO from(InstitutionProfile p, boolean withCampaigns) {
        return from(p, withCampaigns, Map.of());
    }

    public static InstitutionDTO from(InstitutionProfile p, boolean withCampaigns,
                                      Map<Long, CampaignStatsService.CampaignMetric> metricas) {
        var u = p.getUser();
        List<CampaignDTO> camps = withCampaigns && p.getCampaigns() != null
                ? p.getCampaigns().stream()
                    .map(c -> CampaignDTO.from(c, metricas.getOrDefault(c.getId(),
                            CampaignStatsService.CampaignMetric.vazio())))
                    .toList()
                : List.of();
        return new InstitutionDTO(p.getId(), p.getRazaoSocial(), p.getNomeFantasia(), p.getCnpj(),
                p.getEndereco(), p.getWebsite(), p.getFotoUrl(), p.getDescricao(),
                p.getCategoriaAtuacao(), p.getWhatsapp(), p.getPixKey(), p.getPixTitular(),
                p.getAprovado(), p.getMotivoRecusa(),
                u != null ? u.getEmail() : null, u != null ? u.getTelefone() : null,
                u != null ? u.getCidade() : null, u != null ? u.getEstado() : null, camps);
    }
}
