package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String senhaAtual,
        @NotBlank @Size(min = 10, max = 128) String novaSenha,
        @NotBlank String confirmacao) {
}
