package com.conexoessolidarias.service;

import com.conexoessolidarias.model.Notification;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.NotificationRepository;
import com.conexoessolidarias.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Test
    void truncaTipoMensagemELinkNosLimitesDasColunas() {
        var notifRepo = mock(NotificationRepository.class);
        var userRepo = mock(UserRepository.class);
        User user = new User();
        user.setId(1L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        new NotificationService(notifRepo, userRepo)
                .notificar(1L, "t".repeat(50), "m".repeat(400), "l".repeat(300));

        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifRepo).save(captor.capture());
        Notification n = captor.getValue();
        assertEquals(30, n.getTipo().length());
        assertEquals(300, n.getMensagem().length());
        assertEquals(200, n.getLink().length());
    }

    @Test
    void notificarAdminsEnviaParaTodosAtivos() {
        var notifRepo = mock(NotificationRepository.class);
        var userRepo = mock(UserRepository.class);
        User a1 = new User(); a1.setId(1L);
        User a2 = new User(); a2.setId(2L);
        when(userRepo.findAllByTipoAndAtivoTrue("admin")).thenReturn(List.of(a1, a2));
        when(userRepo.findById(1L)).thenReturn(Optional.of(a1));
        when(userRepo.findById(2L)).thenReturn(Optional.of(a2));

        new NotificationService(notifRepo, userRepo)
                .notificarAdmins("teste", "mensagem", "/painel/admin");

        verify(notifRepo, org.mockito.Mockito.times(2)).save(org.mockito.ArgumentMatchers.any());
    }
}
