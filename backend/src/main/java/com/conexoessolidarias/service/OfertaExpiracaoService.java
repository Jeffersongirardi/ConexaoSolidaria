package com.conexoessolidarias.service;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.Oferta;
import com.conexoessolidarias.repository.OfertaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Libera ofertas cuja reserva venceu o prazo de coleta (7 dias), avisando
 * doador e instituição. Roda periodicamente; em testes o método é chamado direto.
 */
@Service
public class OfertaExpiracaoService {

    private static final Logger log = LoggerFactory.getLogger(OfertaExpiracaoService.class);

    private final OfertaRepository ofertaRepository;
    private final NotificationService notificationService;

    public OfertaExpiracaoService(OfertaRepository ofertaRepository,
                                  NotificationService notificationService) {
        this.ofertaRepository = ofertaRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(initialDelayString = "${app.ofertas.expiracao-inicial-ms:300000}",
               fixedDelayString = "${app.ofertas.expiracao-ms:3600000}")
    @Transactional
    public void expirarReservasVencidas() {
        List<Oferta> vencidas = ofertaRepository
                .findByStatusAndPrazoColetaBefore(Oferta.RESERVADA, LocalDate.now());
        for (Oferta o : vencidas) {
            InstitutionProfile anterior = o.getInstituicao();
            o.setStatus(Oferta.DISPONIVEL);
            o.setInstituicao(null);
            o.setReservadaEm(null);
            o.setPrazoColeta(null);
            o.setAceiteColeta(false);
            ofertaRepository.save(o);

            notificationService.notificar(o.getDoador().getId(), "oferta_expirada",
                    "A reserva de \"" + o.getTitulo() + "\" expirou e a oferta voltou a ficar disponível.",
                    "/painel/doador");
            if (anterior != null) {
                notificationService.notificar(anterior.getUser().getId(), "oferta_expirada",
                        "Sua reserva de \"" + o.getTitulo() + "\" expirou e a oferta voltou para o doador.",
                        "/painel/instituicao");
            }
        }
        if (!vencidas.isEmpty()) {
            log.info("Reservas vencidas liberadas: {}", vencidas.size());
        }
    }
}
