package com.conexoessolidarias.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    @Test
    void bloqueiaAposCincoFalhasELimpaNoSucesso() {
        var service = new LoginAttemptService();
        String email = "maria@teste.org";

        for (int i = 0; i < LoginAttemptService.MAX_FALHAS - 1; i++) {
            service.registrarFalha(email);
        }
        assertFalse(service.bloqueado(email), "4 falhas ainda não bloqueiam");

        service.registrarFalha(email);
        assertTrue(service.bloqueado(email), "5ª falha bloqueia");

        service.limpar(email);
        assertFalse(service.bloqueado(email));
    }

    @Test
    void bloqueioEhPorEmail() {
        var service = new LoginAttemptService();
        for (int i = 0; i < LoginAttemptService.MAX_FALHAS; i++) {
            service.registrarFalha("a@teste.org");
        }
        assertTrue(service.bloqueado("A@TESTE.ORG"));
        assertFalse(service.bloqueado("b@teste.org"));
    }

    @Test
    void bloqueioDuraQuinzeMinutosDesdeQuintaFalhaSemProrrogar() {
        var now = new java.util.concurrent.atomic.AtomicLong(0);
        java.time.Clock clock = new java.time.Clock() {
            public java.time.ZoneId getZone() { return java.time.ZoneOffset.UTC; }
            public java.time.Clock withZone(java.time.ZoneId zone) { return this; }
            public java.time.Instant instant() { return java.time.Instant.ofEpochMilli(now.get()); }
        };
        var service = new LoginAttemptService(clock);
        for (int i = 0; i < 4; i++) service.registrarFalha("a@teste.org");
        now.set(10 * 60_000);
        service.registrarFalha("a@teste.org");
        now.set(24 * 60_000);
        assertTrue(service.bloqueado("a@teste.org"));
        service.registrarFalha("a@teste.org");
        now.set(25 * 60_000);
        assertFalse(service.bloqueado("a@teste.org"));
    }
}
