package com.conexoessolidarias.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record OfertaRequest(
        @NotBlank String titulo,
        @NotBlank String descricao,
        String categoria,
        String estadoItem,
        String cidade,
        Boolean precisaColeta,
        String enderecoColeta,
        @NotNull(message = "Informe até quando o item fica disponível")
        @Future(message = "A data de disponibilidade deve ser futura (a partir de amanhã)") LocalDate disponivelAte) {
}
