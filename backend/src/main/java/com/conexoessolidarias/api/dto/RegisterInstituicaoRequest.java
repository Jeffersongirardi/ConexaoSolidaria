package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterInstituicaoRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String senha,
        @NotBlank String cnpj,
        @NotBlank String razaoSocial,
        String nomeFantasia,
        String telefone,
        String whatsapp,
        String endereco,
        String descricao,
        String categoriaAtuacao,
        String pixKey,
        String pixTitular) {
}
