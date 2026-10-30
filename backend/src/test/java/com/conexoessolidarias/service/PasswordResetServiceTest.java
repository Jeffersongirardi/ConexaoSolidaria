package com.conexoessolidarias.service;

import com.conexoessolidarias.model.PasswordResetToken;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    private User user() {
        User u = new User();
        u.setId(1L);
        u.setEmail("maria@teste.org");
        u.setNome("Maria");
        return u;
    }

    @Test
    void salvaApenasOHashComExpiracaoDe30Minutos() {
        var repo = mock(PasswordResetTokenRepository.class);
        when(repo.countByUserIdAndCreatedAtAfter(anyLong(), any())).thenReturn(0L);

        var service = new PasswordResetService(repo);
        String raw = service.solicitar(user()).orElseThrow();

        var captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(repo).save(captor.capture());
        PasswordResetToken salvo = captor.getValue();

        assertEquals(64, raw.length());
        assertEquals(PasswordResetService.hash(raw), salvo.getToken());
        assertNotEquals(raw, salvo.getToken(), "token cru não pode ser persistido");
        assertTrue(salvo.getExpiryDate().isAfter(LocalDateTime.now().plusMinutes(28)));
        assertTrue(salvo.getExpiryDate().isBefore(LocalDateTime.now().plusMinutes(31)));
        verify(repo).marcarAnterioresComoUsados(1L);
    }

    @Test
    void limitaTresPedidosPorHora() {
        var repo = mock(PasswordResetTokenRepository.class);
        when(repo.countByUserIdAndCreatedAtAfter(anyLong(), any())).thenReturn(3L);

        Optional<String> resultado = new PasswordResetService(repo).solicitar(user());

        assertTrue(resultado.isEmpty());
        verify(repo, never()).save(any());
    }

    @Test
    void consumirRejeitaTokenInexistenteUsadoOuExpirado() {
        var repo = mock(PasswordResetTokenRepository.class);
        var service = new PasswordResetService(repo);

        when(repo.findByToken(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.consumir("abc"));

        PasswordResetToken usado = new PasswordResetToken(PasswordResetService.hash("abc"), user());
        usado.setUsed(true);
        when(repo.findByToken(PasswordResetService.hash("abc"))).thenReturn(Optional.of(usado));
        assertThrows(IllegalArgumentException.class, () -> service.consumir("abc"));

        PasswordResetToken expirado = new PasswordResetToken(PasswordResetService.hash("xyz"), user());
        expirado.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(repo.findByToken(PasswordResetService.hash("xyz"))).thenReturn(Optional.of(expirado));
        assertThrows(IllegalArgumentException.class, () -> service.consumir("xyz"));
        assertFalse(expirado.isUsed());
    }
}
