package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DonationRequest(
        @NotNull Long campaignId,
        @NotBlank String item,
        @NotBlank
        @Pattern(regexp = "^[1-9]\\d{0,4}(\\s?\\p{L}{1,10})?$",
                message = "Quantidade inválida (ex.: 10 ou 5 kg)")
        String quantidade,
        String categoria,
        String observacao,
        Boolean precisaColeta,
        String enderecoColeta) {
}
