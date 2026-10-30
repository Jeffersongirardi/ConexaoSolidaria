package com.conexoessolidarias.service;

import com.conexoessolidarias.model.AuthEvent;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.AuthEventRepository;
import org.springframework.stereotype.Service;

/** Auditoria de eventos de autenticação; nunca registra senhas ou tokens. */
@Service
public class AuthEventService {

    private final AuthEventRepository repository;

    public AuthEventService(AuthEventRepository repository) {
        this.repository = repository;
    }

    public void registrar(User user, String email, String evento, String ip, String userAgent) {
        repository.save(new AuthEvent(
                user != null ? user.getId() : null,
                truncar(email, 255),
                truncar(evento, 50),
                truncar(ip, 45),
                truncar(userAgent, 255)));
    }

    private static String truncar(String valor, int limite) {
        if (valor == null || valor.length() <= limite) return valor;
        return valor.substring(0, limite);
    }
}
