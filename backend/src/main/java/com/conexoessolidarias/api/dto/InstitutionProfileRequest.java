package com.conexoessolidarias.api.dto;

public record InstitutionProfileRequest(
        String nomeFantasia,
        String endereco,
        String website,
        String descricao,
        String categoriaAtuacao,
        String whatsapp,
        String pixKey,
        String pixTitular) {
}
