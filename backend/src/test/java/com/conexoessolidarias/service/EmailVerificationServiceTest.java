package com.conexoessolidarias.service;

import com.conexoessolidarias.model.EmailVerificationToken;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.EmailVerificationTokenRepository;
import com.conexoessolidarias.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceTest {

    private User user() {
        User u = new User();
        u.setId(1L);
        u.setEmail("maria@teste.org");
        u.setNome("Maria");
        u.setEmailVerificado(false);
        return u;
    }

    private EmailVerificationService service(EmailVerificationTokenRepository repo, UserRepository userRepo) {
        return new EmailVerificationService(repo, userRepo);
    }

    @Test
    void geraTokenHasheadoCom24hELimitaTresPorHora() {
        var repo = mock(EmailVerificationTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        when(repo.countByUserIdAndCreatedAtAfter(anyLong(), any())).thenReturn(0L);

        String raw = service(repo, userRepo).gerar(user()).orElseThrow();

        var captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(repo).save(captor.capture());
        EmailVerificationToken salvo = captor.getValue();
        assertEquals(PasswordResetService.hash(raw), salvo.getToken());
        assertNotEquals(raw, salvo.getToken());
        assertTrue(salvo.getExpiryDate().isAfter(LocalDateTime.now().plusHours(23)));
        verify(repo).marcarAnterioresComoUsados(1L);

        when(repo.countByUserIdAndCreatedAtAfter(anyLong(), any())).thenReturn(3L);
        assertTrue(service(repo, userRepo).gerar(user()).isEmpty());
        verify(repo, org.mockito.Mockito.times(1)).save(any());
    }

    @Test
    void verificarMarcaUsuarioEUsaTokenUmaVez() {
        var repo = mock(EmailVerificationTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var u = user();
        var token = new EmailVerificationToken(PasswordResetService.hash("abc"), u);
        when(repo.findByToken(PasswordResetService.hash("abc"))).thenReturn(Optional.of(token));

        service(repo, userRepo).verificar("abc");

        assertTrue(Boolean.TRUE.equals(u.getEmailVerificado()));
        assertTrue(token.isUsed());
        verify(userRepo).save(u);
        verify(repo).save(token);
    }

    @Test
    void exigirVerificadoRespeitaFlag() {
        var repo = mock(EmailVerificationTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var s = service(repo, userRepo);
        s.exigirVerificado(user());

        ReflectionTestUtils.setField(s, "requireVerification", true);
        assertThrows(EmailNaoVerificadoException.class, () -> s.exigirVerificado(user()));
        User verificado = user();
        verificado.setEmailVerificado(true);
        s.exigirVerificado(verificado);
    }
}
