package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CampaignRequest(
        @NotBlank String titulo,
        @NotBlank String descricao,
        String categoria,
        @NotBlank String quantidadeAlvo,
        String urgencia,
        Boolean aceitaFinanceiro) {
}
