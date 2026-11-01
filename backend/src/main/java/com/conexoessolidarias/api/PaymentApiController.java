package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.api.dto.PaymentDTO;
import com.conexoessolidarias.api.dto.PaymentRequest;
import com.conexoessolidarias.model.Campaign;
import com.conexoessolidarias.model.Payment;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.CampaignRepository;
import com.conexoessolidarias.repository.PaymentRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.EmailService;
import com.conexoessolidarias.service.EmailVerificationService;
import com.conexoessolidarias.service.NotificationService;
import com.conexoessolidarias.service.PixBrCodeService;
import com.conexoessolidarias.service.QrCodeService;
import com.conexoessolidarias.service.StorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentApiController {

    private final PaymentRepository paymentRepository;
    private final CampaignRepository campaignRepository;
    private final QrCodeService qrCodeService;
    private final PixBrCodeService pixBrCodeService;
    private final NotificationService notificationService;
    private final StorageService storageService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;

    public PaymentApiController(PaymentRepository paymentRepository,
                                CampaignRepository campaignRepository,
                                QrCodeService qrCodeService,
                                PixBrCodeService pixBrCodeService,
                                NotificationService notificationService,
                                StorageService storageService,
                                EmailVerificationService emailVerificationService,
                                EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.campaignRepository = campaignRepository;
        this.qrCodeService = qrCodeService;
        this.pixBrCodeService = pixBrCodeService;
        this.notificationService = notificationService;
        this.storageService = storageService;
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<PaymentDTO> criar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PaymentRequest req) {
        User user = principal.getUser();
        emailVerificationService.exigirVerificado(user);
        String metodo = req.metodo() != null ? req.metodo() : "pix";
        if (!"pix".equals(metodo)) {
            throw new IllegalArgumentException("Somente Pix é aceito");
        }
        Payment payment = new Payment();
        payment.setDoador(user);
        payment.setValor(req.valor());
        payment.setMetodo(metodo);
        if (req.campaignId() != null) {
            Campaign campaign = campaignRepository.findById(req.campaignId())
                    .orElseThrow(() -> new EntityNotFoundException("Campanha não encontrada"));
            if (!Boolean.TRUE.equals(campaign.getAtivo())) {
                throw new IllegalStateException("Campanha inativa");
            }
            if (!Boolean.TRUE.equals(campaign.getAceitaFinanceiro())) {
                throw new IllegalStateException("Campanha não aceita contribuição em valor");
            }
            payment.setCampaign(campaign);
            payment.setInstituicao(campaign.getInstitution());
        } else {
            throw new IllegalArgumentException("campaignId é obrigatório");
        }
        if ("pix".equals(metodo)) {
            // BR Code real gerado da chave da instituição — dinheiro direto a ela, sem gateway.
            payment.setCopiaECola(gerarCopiaECola(payment));
        }
        payment = paymentRepository.save(payment);
        notificationService.notificar(payment.getInstituicao().getUser().getId(), "pagamento_criado",
                payment.getDoador().getNome() + " iniciou um Pix de R$ " + payment.getValor()
                        + " (aguardando pagamento).",
                "/painel/instituicao");
        emailService.evento(payment.getInstituicao().getUser().getEmail(), "Novo Pix iniciado",
                "<p>Novo Pix de R$ " + payment.getValor() + " aguardando pagamento (" + payment.getDoador().getNome() + ").</p>");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDto(payment, buildQrCode(payment)));
    }

    @GetMapping("/meus")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PaymentDTO>> meus(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(paymentRepository
                .findByDoadorIdOrderByDataCriacaoDesc(principal.getUser().getId())
                .stream().map(this::toDto).toList());
    }

    @GetMapping("/recebidos")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PaymentDTO>> recebidos(
            @AuthenticationPrincipal CustomUserDetails principal) {
        var profile = principal.getUser().getInstitutionProfile();
        if (profile == null) throw new EntityNotFoundException("Perfil não encontrado");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(paymentRepository
                .findByInstituicaoIdOrderByDataCriacaoDesc(profile.getId())
                .stream().map(this::toDto).toList());
    }

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<PaymentDTO> detalhe(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Payment payment = requireOwnerOuInstituicao(uuid, principal.getUser());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(toDto(payment, buildQrCode(payment)));
    }

    @GetMapping("/{uuid}/comprovante")
    @Transactional(readOnly = true)
    public ResponseEntity<java.util.Map<String, Object>> comprovante(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Payment payment = requireOwnerOuInstituicao(uuid, principal.getUser());
        if (!"confirmado".equals(payment.getStatus()) && !"recebido".equals(payment.getStatus())) {
            throw new IllegalStateException("Comprovante disponível após a confirmação do pagamento");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(java.util.Map.of(
                "tipo", "financeiro",
                "valor", payment.getValor(),
                "metodo", payment.getMetodo(),
                "instituicao", payment.getInstituicao().getRazaoSocial(),
                "data", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(payment.getDataCriacao()),
                "status", payment.getStatus(),
                "transacaoId", payment.getTransacaoId() != null ? payment.getTransacaoId() : ""));
    }

    @PostMapping("/{uuid}/confirmar-pix")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<PaymentDTO> confirmarPix(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(toDto(processar(uuid, principal.getUser())));
    }

    private PaymentDTO toDto(Payment payment) {
        return toDto(payment, null);
    }

    private PaymentDTO toDto(Payment payment, String qrCode) {
        return PaymentDTO.from(payment, qrCode, storageService::resolveReadUrl);
    }

    private Payment processar(String uuid, User user) {
        Payment payment = requireOwner(uuid, user);
        if (!"pendente".equals(payment.getStatus())) {
            throw new IllegalStateException("Pagamento já processado");
        }
        payment.setStatus("confirmado");
        payment.setTransacaoId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        payment.setDataConfirmacao(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        somarProgresso(payment);
        notificationService.notificar(payment.getInstituicao().getUser().getId(), "nova_doacao",
                user.getNome() + " contribuiu com R$ " + payment.getValor()
                        + " via " + payment.getMetodo() + ".",
                "/painel/instituicao");
        emailService.evento(payment.getInstituicao().getUser().getEmail(), "Pix declarado pelo doador",
                "<p>O doador declarou o Pix de R$ " + payment.getValor() + ". Confira o extrato.</p>");
        return payment;
    }

    private Payment requireOwnerOuInstituicao(String uuid, User user) {
        Payment payment = paymentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
        boolean isDoador = payment.getDoador().getId().equals(user.getId());
        boolean isAdmin = "admin".equals(user.getTipo());
        boolean isInstituicao = "instituicao".equals(user.getTipo())
                && user.getInstitutionProfile() != null
                && payment.getInstituicao().getId().equals(user.getInstitutionProfile().getId());
        if (!isDoador && !isAdmin && !isInstituicao) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        if (isInstituicao && !Boolean.TRUE.equals(user.getInstitutionProfile().getAprovado())) {
            throw new IllegalStateException("Instituição ainda não aprovada");
        }
        return payment;
    }

    private Payment requireOwner(String uuid, User user) {
        Payment payment = paymentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
        if (!payment.getDoador().getId().equals(user.getId()) && !"admin".equals(user.getTipo())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        return payment;
    }

    @PatchMapping("/{uuid}/confirmar-recebimento")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<PaymentDTO> confirmarRecebimentoInstituicao(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Payment payment = requireOwnerOuInstituicao(uuid, principal.getUser());
        if (!"confirmado".equals(payment.getStatus()) && !"pendente".equals(payment.getStatus())) {
            throw new IllegalStateException("Pagamento já processado");
        }
        boolean veioDePendente = "pendente".equals(payment.getStatus());
        if (veioDePendente && payment.getTransacaoId() == null) {
            payment.setTransacaoId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        }
        // Opção A: instituição finaliza em 1 clique — pendente ou confirmado vão direto para recebido.
        payment.setStatus("recebido");
        payment.setDataRecebimento(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        if (veioDePendente) {
            somarProgresso(payment);
        }
        notificationService.notificar(payment.getDoador().getId(), "doacao_confirmada",
                payment.getInstituicao().getRazaoSocial() + " confirmou o recebimento de R$ " + payment.getValor() + ".",
                "/painel/doador");
        emailService.evento(payment.getDoador().getEmail(), "Pix recebido!",
                "<p>" + payment.getInstituicao().getRazaoSocial() + " confirmou o recebimento de R$ " + payment.getValor() + ".</p>");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(toDto(payment));
    }

    @PatchMapping("/{uuid}/rejeitar")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<PaymentDTO> rejeitar(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.getOrDefault("motivo", "").trim() : "";
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo da rejeição é obrigatório");
        }
        Payment payment = requireOwnerOuInstituicao(uuid, principal.getUser());
        if (!"pendente".equals(payment.getStatus()) && !"confirmado".equals(payment.getStatus())) {
            throw new IllegalStateException("Pagamento já finalizado");
        }
        boolean eraConfirmado = "confirmado".equals(payment.getStatus());
        payment.setStatus("rejeitado");
        payment.setMotivoRejeicao(motivo);
        payment.setCopiaECola(null);
        payment = paymentRepository.save(payment);
        if (eraConfirmado) {
            subtrairProgresso(payment);
        }
        notificationService.notificar(payment.getDoador().getId(), "pagamento_rejeitado",
                payment.getInstituicao().getRazaoSocial() + " não identificou o Pix de R$ "
                        + payment.getValor() + ". Motivo: " + motivo,
                "/painel/doador");
        emailService.evento(payment.getDoador().getEmail(), "Pix não identificado",
                "<p>" + payment.getInstituicao().getRazaoSocial() + " não identificou o Pix de R$ "
                        + payment.getValor() + ".</p><p>Motivo: " + motivo + "</p>");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(toDto(payment));
    }

    private void subtrairProgresso(Payment payment) {
        if (payment.getCampaign() != null) {
            Campaign campaign = payment.getCampaign();
            campaign.setProgresso(Math.max(0,
                    (campaign.getProgresso() != null ? campaign.getProgresso() : 0) - 5));
            campaignRepository.save(campaign);
        }
    }

    private void somarProgresso(Payment payment) {        if (payment.getCampaign() != null) {
            Campaign campaign = payment.getCampaign();
            campaign.setProgresso(Math.min(100,
                    (campaign.getProgresso() != null ? campaign.getProgresso() : 0) + 5));
            campaignRepository.save(campaign);
        }
    }

    @PatchMapping("/{uuid}/cancelar")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<PaymentDTO> cancelar(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Payment payment = requireOwner(uuid, principal.getUser());
        if (!"pendente".equals(payment.getStatus())) {
            throw new IllegalStateException("Apenas pagamentos pendentes podem ser cancelados");
        }
        payment.setStatus("cancelado");
        // Invalida o BR Code: cancelado não pode continuar pagável.
        payment.setCopiaECola(null);
        payment = paymentRepository.save(payment);
        notificationService.notificar(payment.getInstituicao().getUser().getId(), "pagamento_cancelado_doador",
                "O doador cancelou o Pix de R$ " + payment.getValor() + ".",
                "/painel/instituicao");
        emailService.evento(payment.getInstituicao().getUser().getEmail(), "Pix cancelado pelo doador",
                "<p>O doador cancelou o Pix de R$ " + payment.getValor() + ".</p>");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(toDto(payment));
    }

    private String gerarCopiaECola(Payment payment) {
        var instituicao = payment.getInstituicao();
        if (instituicao == null || instituicao.getPixKey() == null || instituicao.getPixKey().isBlank()) {
            throw new IllegalStateException("Instituição não cadastrou chave PIX — fale com ela para contribuir por outro meio");
        }
        String cidade = instituicao.getUser() != null ? instituicao.getUser().getCidade() : null;
        try {
            return pixBrCodeService.gerarCopiaECola(instituicao.getPixKey(), payment.getValor(),
                    instituicao.getRazaoSocial(), cidade, null);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Chave PIX da instituição é inválida — fale com ela para contribuir por outro meio");
        }
    }

    private String buildQrCode(Payment payment) {
        if (!"pix".equals(payment.getMetodo())
                || !"pendente".equals(payment.getStatus())) {
            return null;
        }
        String copiaECola = payment.getCopiaECola();
        if (copiaECola == null || copiaECola.isBlank()) return null;
        return qrCodeService.gerarQrCodeBase64(copiaECola, 250, 250);
    }
}
