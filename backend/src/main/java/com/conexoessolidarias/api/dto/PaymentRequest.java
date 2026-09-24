package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        Long campaignId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotBlank String metodo) {
}
