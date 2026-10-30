package com.conexoessolidarias.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SenhaForteTest {

    @Test
    void aceitaFraseLonga() {
        assertDoesNotThrow(() -> SenhaForte.validar("Minha frase longa 2026", "maria@teste.org", "Maria"));
        assertDoesNotThrow(() -> SenhaForte.validar("SenhaForte2026", null, null));
    }

    @Test
    void rejeitaCurta() {
        assertThrows(IllegalArgumentException.class,
                () -> SenhaForte.validar("Curta1", "maria@teste.org", "Maria"));
    }

    @Test
    void rejeitaComum() {
        assertThrows(IllegalArgumentException.class,
                () -> SenhaForte.validar("1234567890", "maria@teste.org", "Maria"));
        assertThrows(IllegalArgumentException.class,
                () -> SenhaForte.validar("Conexoes123", "maria@teste.org", "Maria"));
    }

    @Test
    void rejeitaIgualAoEmailOuNome() {
        assertThrows(IllegalArgumentException.class,
                () -> SenhaForte.validar("maria@teste.org", "maria@teste.org", "Outro"));
        assertThrows(IllegalArgumentException.class,
                () -> SenhaForte.validar("maria", "maria@teste.org", "Outro"));
        assertThrows(IllegalArgumentException.class,
                () -> SenhaForte.validar("maria", "x@teste.org", "Maria"));
    }

    @Test
    void rejeitaVazia() {
        assertThrows(IllegalArgumentException.class, () -> SenhaForte.validar("   ", null, null));
    }
}
