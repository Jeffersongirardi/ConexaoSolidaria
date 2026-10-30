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
}
