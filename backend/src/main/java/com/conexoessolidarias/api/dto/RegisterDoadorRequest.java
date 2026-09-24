package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDoadorRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String senha,
        @Pattern(regexp = "^$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", message = "Telefone inválido") String telefone,
        @Pattern(regexp = "^$|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$|^\\d{11}$", message = "CPF inválido") String cpf,
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "Data deve ser AAAA-MM-DD") String dataNascimento,
        @Pattern(regexp = "^$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", message = "WhatsApp inválido") String whatsapp,
        @Pattern(regexp = "^$|^\\d{5}-?\\d{3}$", message = "CEP inválido") String cep,
        String cidade,
        @Pattern(regexp = "^$|^[A-Za-z]{2}$", message = "UF deve ter 2 letras") String estado) {
}
