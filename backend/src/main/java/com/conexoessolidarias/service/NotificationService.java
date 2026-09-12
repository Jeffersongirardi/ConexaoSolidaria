package com.conexoessolidarias.service;

import com.conexoessolidarias.model.Notification;
import com.conexoessolidarias.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notificar(Long usuarioId, String tipo, String mensagem, String link) {
        Notification n = new Notification();
        n.setTipo(tipo);
        n.setMensagem(mensagem);
        n.setLink(link);
        notificationRepository.save(n);
    }

    @Transactional
    public void marcarTodasComoLidas(Long usuarioId) {
        notificationRepository.marcarTodasComoLidas(usuarioId);
    }

    public long contarNaoLidas(Long usuarioId) {
        return notificationRepository.countByUserIdAndLidaFalse(usuarioId);
    }
}
