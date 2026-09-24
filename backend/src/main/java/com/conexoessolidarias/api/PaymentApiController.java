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
import com.conexoessolidarias.service.NotificationService;
import com.conexoessolidarias.service.QrCodeService;
import com.conexoessolidarias.service.StorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentApiController {

    private final PaymentRepository paymentRepository;
    private final CampaignRepository campaignRepository;
    private final QrCodeService qrCodeService;
    private final NotificationService notificationService;
    private final StorageService storageService;

    public PaymentApiController(PaymentRepository paymentRepository,
                                CampaignRepository campaignRepository,
                                QrCodeService qrCodeService,
                                NotificationService notificationService,
                                StorageService storageService) {
        this.paymentRepository = paymentRepository;
        this.campaignRepository = campaignRepository;
        this.qrCodeService = qrCodeService;
        this.notificationService = notificationService;
        this.storageService = storageService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<PaymentDTO> criar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PaymentRequest req) {
        User user = principal.getUser();
        String metodo = req.metodo() != null ? req.metodo() : "pix";
        if (!List.of("pix", "cartao", "transferencia").contains(metodo)) {
            throw new IllegalArgumentException("Método inválido");
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
            payment.setCampaign(campaign);
            payment.setInstituicao(campaign.getInstitution());
        } else {
            throw new IllegalArgumentException("campaignId é obrigatório");
        }
        payment = paymentRepository.save(payment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentDTO.from(payment, buildQrCode(payment)));
    }

    @GetMapping("/meus")
    @PreAuthorize("hasRole('DOADOR')")
    public ResponseEntity<List<PaymentDTO>> meus(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(paymentRepository
                .findByDoadorIdOrderByDataCriacaoDesc(principal.getUser().getId())
                .stream().map(PaymentDTO::from).toList());
    }

    @GetMapping("/recebidos")
    @PreAuthorize("hasRole('INSTITUICAO')")
    public ResponseEntity<List<PaymentDTO>> recebidos(
            @AuthenticationPrincipal CustomUserDetails principal) {
        var profile = principal.getUser().getInstitutionProfile();
        if (profile == null) throw new EntityNotFoundException("Perfil não encontrado");
        return ResponseEntity.ok(paymentRepository
                .findByInstituicaoIdOrderByDataCriacaoDesc(profile.getId())
                .stream().map(PaymentDTO::from).toList());
    }

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<PaymentDTO> detalhe(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Payment payment = requireOwner(uuid, principal.getUser());
        return ResponseEntity.ok(PaymentDTO.from(payment, buildQrCode(payment)));
    }

    @GetMapping("/{uuid}/comprovante")
    @Transactional(readOnly = true)
    public ResponseEntity<java.util.Map<String, Object>> comprovante(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Payment payment = requireOwner(uuid, principal.getUser());
        return ResponseEntity.ok(java.util.Map.of(
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
        return ResponseEntity.ok(PaymentDTO.from(processar(uuid, principal.getUser(), null)));
    }

    @PostMapping("/{uuid}/confirmar-cartao")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<PaymentDTO> confirmarCartao(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody(required = false) java.util.Map<String, String> dados) {
        return ResponseEntity.ok(PaymentDTO.from(processar(uuid, principal.getUser(), null)));
    }

    @PostMapping(value = "/{uuid}/confirmar-transferencia",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<PaymentDTO> confirmarTransferencia(
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(value = "comprovante", required = false) MultipartFile comprovante) {
        String url = comprovante != null ? storageService.save(comprovante, "comprovantes") : null;
        return ResponseEntity.ok(PaymentDTO.from(processar(uuid, principal.getUser(), url)));
    }

    private Payment processar(String uuid, User user, String comprovanteUrl) {
        Payment payment = requireOwner(uuid, user);
        if (!"pendente".equals(payment.getStatus())) {
            throw new IllegalStateException("Pagamento já processado");
        }
        if (comprovanteUrl != null) payment.setComprovanteUrl(comprovanteUrl);
        payment.setStatus("confirmado");
        payment.setTransacaoId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        payment.setDataConfirmacao(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        if (payment.getCampaign() != null) {
            Campaign campaign = payment.getCampaign();
            campaign.setProgresso(Math.min(100,
                    (campaign.getProgresso() != null ? campaign.getProgresso() : 0) + 5));
            campaignRepository.save(campaign);
        }
        notificationService.notificar(payment.getInstituicao().getUser().getId(), "nova_doacao",
                user.getNome() + " contribuiu com R$ " + payment.getValor()
                        + " via " + payment.getMetodo() + ".",
                "/painel/instituicao");
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

    private String buildQrCode(Payment payment) {
        if (!"pix".equals(payment.getMetodo())
                || !"pendente".equals(payment.getStatus())) {
            return null;
        }
        String pixKey = payment.getInstituicao().getPixKey();
        if (pixKey == null) pixKey = "chave@exemplo.org";
        return qrCodeService.gerarQrCodeBase64(
                "pix://" + pixKey + "?amount=" + payment.getValor(), 250, 250);
    }
}
