package com.conexoessolidarias.service;

import com.conexoessolidarias.model.Notification;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.NotificationRepository;
import com.conexoessolidarias.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notificar(Long usuarioId, String tipo, String mensagem, String link) {
        User user = userRepository.findById(usuarioId).orElse(null);
        if (user == null) return;
        Notification n = new Notification(user, truncar(tipo, 30), truncar(mensagem, 300), truncar(link, 200));
        notificationRepository.save(n);
    }

    /** Avisa todos os admins ativos (novas instituições, ofertas e mensagens de contato). */
    public void notificarAdmins(String tipo, String mensagem, String link) {
        for (User admin : userRepository.findAllByTipoAndAtivoTrue("admin")) {
            notificar(admin.getId(), tipo, mensagem, link);
        }
    }

    private static String truncar(String valor, int limite) {
        if (valor == null || valor.length() <= limite) return valor;
        return valor.substring(0, limite - 1) + "…";
    }

    @Transactional
    public void marcarTodasComoLidas(Long usuarioId) {
        notificationRepository.marcarTodasComoLidas(usuarioId);
    }

    public long contarNaoLidas(Long usuarioId) {
        return notificationRepository.countByUserIdAndLidaFalse(usuarioId);
    }
}
