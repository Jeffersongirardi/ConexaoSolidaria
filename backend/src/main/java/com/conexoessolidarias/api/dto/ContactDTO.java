package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.ContactMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record ContactDTO(
        Long id,
        String nome,
        String email,
        String assunto,
        String mensagem,
        Boolean lido,
        LocalDateTime dataEnvio) {

    public static ContactDTO from(ContactMessage m) {
        return new ContactDTO(m.getId(), m.getNome(), m.getEmail(), m.getAssunto(),
                m.getMensagem(), m.getLido(), m.getDataEnvio());
    }

    public record ContactRequest(
            @NotBlank String nome,
            @NotBlank @Email String email,
            String assunto,
            @NotBlank String mensagem) {
    }
}
