package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.Notification;
import java.time.LocalDateTime;

public record NotificationDTO(
        Long id,
        String tipo,
        String mensagem,
        String link,
        Boolean lida,
        LocalDateTime dataCriacao) {

    public static NotificationDTO from(Notification n) {
        return new NotificationDTO(n.getId(), n.getTipo(), n.getMensagem(), n.getLink(),
                n.getLida(), n.getDataCriacao());
    }
}
