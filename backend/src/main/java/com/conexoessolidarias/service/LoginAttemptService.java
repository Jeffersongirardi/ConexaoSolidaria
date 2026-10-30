package com.conexoessolidarias.service;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lockout por conta: 5 falhas em 15 minutos bloqueiam novas tentativas
 * temporariamente. Em memória (uma instância); a auditoria fica no banco.
 */
@Service
public class LoginAttemptService {

    public static final int MAX_FALHAS = 5;
    public static final long JANELA_MS = 15 * 60 * 1000L;

    private final ConcurrentHashMap<String, Janela> tentativas = new ConcurrentHashMap<>();

    private static class Janela {
        final AtomicLong inicio = new AtomicLong(System.currentTimeMillis());
        final AtomicInteger falhas = new AtomicInteger(0);
    }

    public boolean bloqueado(String email) {
        Janela j = tentativas.get(chave(email));
        if (j == null) return false;
        if (System.currentTimeMillis() - j.inicio.get() > JANELA_MS) {
            tentativas.remove(chave(email));
            return false;
        }
        return j.falhas.get() >= MAX_FALHAS;
    }

    public void registrarFalha(String email) {
        String k = chave(email);
        Janela j = tentativas.computeIfAbsent(k, x -> new Janela());
        long agora = System.currentTimeMillis();
        if (agora - j.inicio.get() > JANELA_MS) {
            j.inicio.set(agora);
            j.falhas.set(1);
            return;
        }
        j.falhas.incrementAndGet();
    }

    public void limpar(String email) {
        tentativas.remove(chave(email));
    }

    private String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
