package com.conexoessolidarias.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.conexoessolidarias.model.*;
import com.conexoessolidarias.repository.*;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.*;
import org.springframework.security.access.AccessDeniedException;

class PrivateUploadAccessTest {
    private User user(long id) { var u = new User(); u.setId(id); u.setTipo("doador"); return u; }

    @Test void receiptIsResolvedOnlyForOwnerAndDatabasePathDoesNotChange() {
        var repository = mock(PaymentRepository.class);
        var storage = mock(StorageService.class);
        var payment = new Payment(); payment.setDoador(user(1)); payment.setMetodo("transferencia");
        payment.setComprovanteUrl("/uploads/comprovantes/example.png");
        when(repository.findByUuid("test")).thenReturn(Optional.of(payment));
        when(storage.resolveReadUrl(payment.getComprovanteUrl())).thenReturn("https://signed.example/receipt");
        var controller = new PaymentApiController(repository, mock(CampaignRepository.class), mock(QrCodeService.class),
                mock(PixBrCodeService.class), mock(NotificationService.class), storage, mock(EmailVerificationService.class));
        assertThrows(AccessDeniedException.class, () -> controller.detalhe("test", new CustomUserDetails(user(2))));
        verifyNoInteractions(storage);
        var result = controller.detalhe("test", new CustomUserDetails(user(1))).getBody();
        assertEquals("https://signed.example/receipt", result.comprovanteUrl());
        assertEquals("/uploads/comprovantes/example.png", payment.getComprovanteUrl());
    }

    @Test void donationUpdatesAreResolvedOnlyAfterParticipantCheck() {
        var repository = mock(DonationRepository.class);
        var storage = mock(StorageService.class);
        var donation = new Donation(); donation.setDoador(user(1));
        var update = new DonationUpdate(donation, "Entregue"); update.setFotoUrl("/uploads/updates/example.png");
        donation.setUpdates(List.of(update));
        when(repository.findById(1L)).thenReturn(Optional.of(donation));
        when(storage.resolveReadUrl(update.getFotoUrl())).thenReturn("https://signed.example/update");
        var controller = new DonationApiController(repository, mock(DonationUpdateRepository.class), mock(CampaignRepository.class),
                mock(NotificationService.class), mock(EmailService.class), storage, mock(EmailVerificationService.class));
        assertThrows(AccessDeniedException.class, () -> controller.detalhe(1L, new CustomUserDetails(user(2))));
        verifyNoInteractions(storage);
        assertEquals("https://signed.example/update", controller.detalhe(1L, new CustomUserDetails(user(1)))
                .getBody().updates().get(0).fotoUrl());
        assertEquals("/uploads/updates/example.png", update.getFotoUrl());
    }
}
