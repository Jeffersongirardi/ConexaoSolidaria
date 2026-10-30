package com.conexoessolidarias.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataSeederTest {

    @Test
    void producaoRejeitaSenhaPadraoFracaOuIgualAoEmail() {
        assertThrows(IllegalStateException.class,
                () -> DataSeeder.validarAdminProducao("admin@teste.org", null));
        assertThrows(IllegalStateException.class,
                () -> DataSeeder.validarAdminProducao("admin@teste.org", "admin123"));
        assertThrows(IllegalStateException.class,
                () -> DataSeeder.validarAdminProducao("admin@teste.org", "curta12"));
        assertThrows(IllegalStateException.class,
                () -> DataSeeder.validarAdminProducao("admin@teste.org", "admin@teste.org"));
    }

    @Test
    void producaoAceitaSenhaForte() {
        assertDoesNotThrow(() -> DataSeeder.validarAdminProducao("admin@teste.org", "AdminForte#2026"));
    }
}
