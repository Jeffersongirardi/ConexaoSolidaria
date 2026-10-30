package com.conexoessolidarias.service;

import com.conexoessolidarias.model.EmailLembrete;
import com.conexoessolidarias.model.Oferta;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.EmailLembreteRepository;
import com.conexoessolidarias.repository.OfertaRepository;
import com.conexoessolidarias.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Lembretes diários idempotentes: verificação pendente e reserva de oferta vencendo. */
@Service
public class LembreteEmailService {

    private static final String VERIFICACAO = "verificacao_pendente";
    private static final String OFERTA = "oferta_vencendo";

    private final UserRepository userRepository;
    private final OfertaRepository ofertaRepository;
    private final EmailLembreteRepository lembreteRepository;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;

    @Value("${app.security.require-email-verification:false}")
    private boolean exigirVerificacao;

    public LembreteEmailService(UserRepository userRepository, OfertaRepository ofertaRepository,
                                EmailLembreteRepository lembreteRepository,
                                EmailVerificationService emailVerificationService,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.ofertaRepository = ofertaRepository;
        this.lembreteRepository = lembreteRepository;
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
    }

    @Scheduled(initialDelayString = "${app.lembretes.inicial-ms:600000}",
               fixedDelayString = "${app.lembretes.intervalo-ms:86400000}")
    @Transactional
    public void enviarLembretes() {
        if (exigirVerificacao) {
            for (User u : userRepository.findByEmailVerificadoFalseAndDataCadastroBefore(
                    LocalDateTime.now().minusHours(24))) {
                if (lembreteRepository.existsByTipoAndReferenciaIdAndUserId(
                        VERIFICACAO, u.getId(), u.getId())) continue;
                emailVerificationService.gerar(u).ifPresent(raw -> {
                    emailService.evento(u.getEmail(), "Confirme seu e-mail",
                            "<p>Falta pouco! <a href=\"" + linkVerificacao(raw) + "\">Confirme seu e-mail</a> (expira em 24h).</p>");
                    lembreteRepository.save(new EmailLembrete(VERIFICACAO, u.getId(), u.getId()));
                });
            }
        }
        LocalDate hoje = LocalDate.now();
        for (Oferta o : ofertaRepository.findByStatusAndPrazoColetaBetween(
                Oferta.RESERVADA, hoje, hoje.plusDays(2))) {
            if (o.getInstituicao() == null || o.getPrazoColeta() == null) continue;
            Long userId = o.getInstituicao().getUser().getId();
            if (lembreteRepository.existsByTipoAndReferenciaIdAndUserId(OFERTA, o.getId(), userId)) continue;
            emailService.evento(o.getInstituicao().getUser().getEmail(), "Reserva de oferta vencendo",
                    "<p>A reserva de \"" + o.getTitulo() + "\" vence em " + o.getPrazoColeta()
                            + ". Combine a coleta com o doador.</p>");
            lembreteRepository.save(new EmailLembrete(OFERTA, o.getId(), userId));
        }
    }

    private String linkVerificacao(String raw) {
        return (System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:3000"))
                + "/verificar-email?token=" + raw;
    }
}
