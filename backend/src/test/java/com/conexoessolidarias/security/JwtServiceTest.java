package com.conexoessolidarias.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    @Test
    void rejeitaSegredoComMenosDe32Bytes() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", "segredo-curto");
        assertThrows(IllegalStateException.class, service::validarSegredo);
    }

    @Test
    void aceitaSegredoCom32BytesOuMais() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", "x".repeat(32));
        assertDoesNotThrow(service::validarSegredo);
    }
}
