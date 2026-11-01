package com.conexoessolidarias.service;

import org.springframework.stereotype.Service;
import java.time.Clock;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/** Contagem por e-mail, em memória: cinco falhas bloqueiam por 15 minutos. */
@Service
public class LoginAttemptService {
    public static final int MAX_FALHAS = 5;
    public static final long JANELA_MS = 15 * 60 * 1000L;
    private final Clock clock;
    private final ConcurrentHashMap<String, Janela> tentativas = new ConcurrentHashMap<>();
    private record Janela(long inicio, int falhas, long bloqueadoAte) {}
    public LoginAttemptService() { this(Clock.systemUTC()); }
    LoginAttemptService(Clock clock) { this.clock = clock; }
    private String chave(String email) { return email == null ? "" : email.trim().toLowerCase(Locale.ROOT); }
    private Janela atual(String email) {
        return tentativas.computeIfPresent(chave(email), (key, j) ->
            clock.millis() >= (j.bloqueadoAte() > 0 ? j.bloqueadoAte() : j.inicio() + JANELA_MS) ? null : j);
    }
    public boolean bloqueado(String email) {
        Janela j = atual(email);
        return j != null && j.bloqueadoAte() > clock.millis();
    }
    public void registrarFalha(String email) {
        tentativas.compute(chave(email), (key, j) -> {
            long now = clock.millis();
            if (j != null && j.bloqueadoAte() > now) return j;
            if (j == null || now >= (j.bloqueadoAte() > 0 ? j.bloqueadoAte() : j.inicio() + JANELA_MS))
                return new Janela(now, 1, 0);
            int count = j.falhas() + 1;
            return new Janela(j.inicio(), count, count >= MAX_FALHAS ? now + JANELA_MS : 0);
        });
    }
    public String mensagemFalha(String email) {
        Janela j = atual(email);
        if (j != null && j.bloqueadoAte() > clock.millis()) {
            long minutes = Math.max(1, (j.bloqueadoAte() - clock.millis() + 59999) / 60000);
            return "Muitas tentativas de login. Tente novamente em " + minutes + " minuto(s).";
        }
        int remaining = j == null ? MAX_FALHAS : MAX_FALHAS - j.falhas();
        return "E-mail ou senha inválidos." + (remaining <= 2 ? " Tentativas restantes: " + remaining + "." : "");
    }
    public void limpar(String email) { tentativas.remove(chave(email)); }
}
