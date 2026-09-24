package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String senhaAtual,
        @NotBlank @Size(min = 6, max = 100) String novaSenha,
        @NotBlank String confirmacao) {
}
