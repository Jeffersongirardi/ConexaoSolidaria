package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String nome,
        String telefone,
        String whatsapp,
        String cep,
        String cidade,
        String estado) {
}
