package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.DonationDTO;
import com.conexoessolidarias.api.dto.DonationRequest;
import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.model.Campaign;
import com.conexoessolidarias.model.Donation;
import com.conexoessolidarias.model.DonationUpdate;
import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.CampaignRepository;
import com.conexoessolidarias.repository.DonationRepository;
import com.conexoessolidarias.repository.DonationUpdateRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.EmailService;
import com.conexoessolidarias.service.EmailVerificationService;
import com.conexoessolidarias.service.NotificationService;
import com.conexoessolidarias.service.StorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/donations")
public class DonationApiController {

    private final DonationRepository donationRepository;
    private final DonationUpdateRepository updateRepository;
    private final CampaignRepository campaignRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final StorageService storageService;
    private final EmailVerificationService emailVerificationService;

    public DonationApiController(DonationRepository donationRepository,
                                 DonationUpdateRepository updateRepository,
                                 CampaignRepository campaignRepository,
                                 NotificationService notificationService,
                                 EmailService emailService,
                                 StorageService storageService,
                                 EmailVerificationService emailVerificationService) {
        this.donationRepository = donationRepository;
        this.updateRepository = updateRepository;
        this.campaignRepository = campaignRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.storageService = storageService;
        this.emailVerificationService = emailVerificationService;
    }

    private DonationDTO toDto(Donation donation) {
        return DonationDTO.from(donation, storageService::resolveReadUrl);
    }

    @PostMapping
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<DonationDTO> criar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody DonationRequest req) {
        User user = principal.getUser();
        emailVerificationService.exigirVerificado(user);
        Campaign campaign = campaignRepository.findById(req.campaignId())
                .orElseThrow(() -> new EntityNotFoundException("Campanha não encontrada"));
        if (!Boolean.TRUE.equals(campaign.getAtivo())) {
            throw new IllegalStateException("Campanha inativa");
        }
        Donation donation = new Donation();
        donation.setDoador(user);
        donation.setCampaign(campaign);
        donation.setItem(req.item());
        donation.setQuantidade(req.quantidade());
        donation.setCategoria(com.conexoessolidarias.model.Categoria.normalizar(
                req.categoria() != null ? req.categoria() : campaign.getCategoria()));
        donation.setObservacao(req.observacao());
        boolean precisaColeta = Boolean.TRUE.equals(req.precisaColeta());
        if (precisaColeta && (req.enderecoColeta() == null || req.enderecoColeta().isBlank())) {
            throw new IllegalArgumentException("Endereço de coleta é obrigatório quando precisa de coleta");
        }
        donation.setPrecisaColeta(precisaColeta);
        donation.setEnderecoColeta(precisaColeta ? req.enderecoColeta() : null);
        donation = donationRepository.save(donation);

        notificationService.notificar(
                campaign.getInstitution().getUser().getId(), "nova_doacao",
                user.getNome() + " quer doar " + donation.getQuantidade()
                        + " de " + donation.getItem() + " para \"" + campaign.getTitulo() + "\"",
                "/painel/instituicao");
        emailService.notificarNovaDoacao(user.getEmail(), user.getNome(),
                campaign.getInstitution().getRazaoSocial(), donation.getItem());

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(donation));
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DonationDTO>> minhas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(donationRepository
                .findByDoadorIdOrderByDataIntencaoDesc(principal.getUser().getId())
                .stream().map(this::toDto).toList());
    }

    @GetMapping("/recebidas")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DonationDTO>> recebidas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        InstitutionProfile profile = requireApprovedProfile(principal.getUser());
        List<Long> ids = campaignRepository.findByInstitutionIdOrderByDataCriacaoDesc(profile.getId())
                .stream().map(Campaign::getId).toList();
        if (ids.isEmpty()) return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Collections.emptyList());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(donationRepository
                .findByCampaignIdInOrderByDataIntencaoDesc(ids)
                .stream().map(this::toDto).toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<DonationDTO> detalhe(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doação não encontrada"));
        assertParticipant(donation, principal.getUser());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(toDto(donation));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<DonationDTO> confirmar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doação não encontrada"));
        User user = principal.getUser();
        InstitutionProfile profile = requireApprovedProfile(user);
        if (!donation.getCampaign().getInstitution().getId().equals(profile.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        if (!"pendente".equals(donation.getStatus())) {
            throw new IllegalStateException("Apenas doações pendentes podem ser confirmadas");
        }
        donation.setStatus("recebido");
        donation.setDataRecebimento(LocalDateTime.now());
        donationRepository.save(donation);

        Campaign campaign = donation.getCampaign();
        campaign.setProgresso(Math.min(100,
                (campaign.getProgresso() != null ? campaign.getProgresso() : 0) + 10));
        campaignRepository.save(campaign);

        notificationService.notificar(donation.getDoador().getId(), "doacao_confirmada",
                "Sua doação de " + donation.getQuantidade() + " de " + donation.getItem()
                        + " foi confirmada como recebida por " + profile.getRazaoSocial() + "!",
                "/painel/doador");
        emailService.notificarDoacaoConfirmada(donation.getDoador().getEmail(),
                donation.getDoador().getNome(), profile.getRazaoSocial(), donation.getItem());

        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(toDto(donation));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<DonationDTO> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doação não encontrada"));
        if (!donation.getDoador().getId().equals(principal.getUser().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        if (!"pendente".equals(donation.getStatus())) {
            throw new IllegalStateException("Apenas doações pendentes podem ser canceladas");
        }
        donation.setStatus("cancelado");
        donation = donationRepository.save(donation);
        notificationService.notificar(donation.getCampaign().getInstitution().getUser().getId(),
                "doacao_cancelada_doador",
                donation.getDoador().getNome() + " cancelou a doação de " + donation.getItem() + ".",
                "/painel/instituicao");
        emailService.evento(donation.getCampaign().getInstitution().getUser().getEmail(),
                "Doação cancelada pelo doador", "<p>" + donation.getDoador().getNome() + " cancelou a doação de " + donation.getItem() + ".</p>");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(toDto(donation));
    }

    @PatchMapping("/{id}/rejeitar")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<DonationDTO> rejeitar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.getOrDefault("motivo", "").trim() : "";
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo da rejeição é obrigatório");
        }
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doação não encontrada"));
        InstitutionProfile profile = requireApprovedProfile(principal.getUser());
        if (!donation.getCampaign().getInstitution().getId().equals(profile.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        if (!"pendente".equals(donation.getStatus())) {
            throw new IllegalStateException("Apenas doações pendentes podem ser rejeitadas");
        }
        donation.setStatus("rejeitado");
        donation.setMotivoRejeicao(motivo);
        donation = donationRepository.save(donation);
        notificationService.notificar(donation.getDoador().getId(), "doacao_rejeitada",
                profile.getRazaoSocial() + " não pôde receber sua doação de " + donation.getItem()
                        + ". Motivo: " + motivo,
                "/painel/doador");
        emailService.evento(donation.getDoador().getEmail(), "Doação não aceita pela instituição",
                "<p>Olá " + donation.getDoador().getNome() + "! " + profile.getRazaoSocial()
                        + " não pôde receber sua doação de " + donation.getItem() + ".</p><p>Motivo: " + motivo + "</p>");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(toDto(donation));
    }

    @PostMapping(value = "/{id}/atualizacoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<DonationDTO> adicionarAtualizacao(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String mensagem,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doação não encontrada"));
        User user = principal.getUser();
        requireApprovedProfile(user);
        if (!donation.getCampaign().getInstitution().getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        if ("cancelado".equals(donation.getStatus())) {
            throw new IllegalStateException("Doação cancelada não recebe atualizações");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("Mensagem da atualização é obrigatória");
        }
        DonationUpdate update = new DonationUpdate(donation, mensagem);
        if (foto != null && !foto.isEmpty()) {
            update.setFotoUrl(storageService.save(foto, "updates"));
        }
        updateRepository.save(update);
        notificationService.notificar(donation.getDoador().getId(), "doacao_atualizada",
                donation.getCampaign().getInstitution().getRazaoSocial() + " postou uma atualização sobre sua doação de "
                        + donation.getItem() + ".",
                "/painel/doador");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDto(donationRepository.findById(id).orElseThrow()));
    }

    private void assertParticipant(Donation donation, User user) {
        boolean doador = donation.getDoador().getId().equals(user.getId());
        boolean instituicao = "instituicao".equals(user.getTipo())
                && user.getInstitutionProfile() != null
                && Boolean.TRUE.equals(user.getInstitutionProfile().getAprovado())
                && donation.getCampaign().getInstitution().getId()
                    .equals(user.getInstitutionProfile().getId());
        boolean admin = "admin".equals(user.getTipo());
        if (!doador && !instituicao && !admin) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
    }

    private InstitutionProfile requireProfile(User user) {
        InstitutionProfile profile = user.getInstitutionProfile();
        if (profile == null) throw new EntityNotFoundException("Perfil de instituição não encontrado");
        return profile;
    }

    private InstitutionProfile requireApprovedProfile(User user) {
        InstitutionProfile profile = requireProfile(user);
        if (!Boolean.TRUE.equals(profile.getAprovado())) {
            throw new IllegalStateException("Instituição ainda não aprovada");
        }
        return profile;
    }
}
