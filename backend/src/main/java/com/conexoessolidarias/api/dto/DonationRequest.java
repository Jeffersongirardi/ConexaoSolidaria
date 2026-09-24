package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DonationRequest(
        @NotNull Long campaignId,
        @NotBlank String item,
        @NotBlank String quantidade,
        String categoria,
        String observacao) {
}
