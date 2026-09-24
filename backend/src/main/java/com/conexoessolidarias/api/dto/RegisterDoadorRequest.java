package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDoadorRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String senha,
        String telefone,
        String cpf,
        String dataNascimento,
        String whatsapp,
        String cep,
        String cidade,
        String estado) {
}
