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
import com.conexoessolidarias.service.NotificationService;
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
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/donations")
public class DonationApiController {

    private final DonationRepository donationRepository;
    private final DonationUpdateRepository updateRepository;
    private final CampaignRepository campaignRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final StorageService storageService;

    public DonationApiController(DonationRepository donationRepository,
                                 DonationUpdateRepository updateRepository,
                                 CampaignRepository campaignRepository,
                                 NotificationService notificationService,
                                 EmailService emailService,
                                 StorageService storageService) {
        this.donationRepository = donationRepository;
        this.updateRepository = updateRepository;
        this.campaignRepository = campaignRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.storageService = storageService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<DonationDTO> criar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody DonationRequest req) {
        User user = principal.getUser();
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
        donation.setCategoria(req.categoria() != null ? req.categoria() : "outro");
        donation.setObservacao(req.observacao());
        donation = donationRepository.save(donation);

        notificationService.notificar(
                campaign.getInstitution().getUser().getId(), "nova_doacao",
                user.getNome() + " quer doar " + donation.getQuantidade()
                        + " de " + donation.getItem() + " para \"" + campaign.getTitulo() + "\"",
                "/painel/instituicao");
        emailService.notificarNovaDoacao(user.getEmail(), user.getNome(),
                campaign.getInstitution().getRazaoSocial(), donation.getItem());

        return ResponseEntity.status(HttpStatus.CREATED).body(DonationDTO.from(donation));
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('DOADOR')")
    public ResponseEntity<List<DonationDTO>> minhas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(donationRepository
                .findByDoadorIdOrderByDataIntencaoDesc(principal.getUser().getId())
                .stream().map(DonationDTO::from).toList());
    }

    @GetMapping("/recebidas")
    @PreAuthorize("hasRole('INSTITUICAO')")
    public ResponseEntity<List<DonationDTO>> recebidas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        InstitutionProfile profile = requireProfile(principal.getUser());
        List<Long> ids = campaignRepository.findByInstitutionIdOrderByDataCriacaoDesc(profile.getId())
                .stream().map(Campaign::getId).toList();
        if (ids.isEmpty()) return ResponseEntity.ok(Collections.emptyList());
        return ResponseEntity.ok(donationRepository
                .findByCampaignIdInOrderByDataIntencaoDesc(ids)
                .stream().map(DonationDTO::from).toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<DonationDTO> detalhe(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doação não encontrada"));
        assertParticipant(donation, principal.getUser());
        return ResponseEntity.ok(DonationDTO.from(donation));
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
        InstitutionProfile profile = requireProfile(user);
        if (!donation.getCampaign().getInstitution().getId().equals(profile.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
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

        return ResponseEntity.ok(DonationDTO.from(donation));
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
        return ResponseEntity.ok(DonationDTO.from(donationRepository.save(donation)));
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
        if (!donation.getCampaign().getInstitution().getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        DonationUpdate update = new DonationUpdate(donation, mensagem);
        if (foto != null && !foto.isEmpty()) {
            update.setFotoUrl(storageService.save(foto, "updates"));
        }
        updateRepository.save(update);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DonationDTO.from(donationRepository.findById(id).orElseThrow()));
    }

    private void assertParticipant(Donation donation, User user) {
        boolean doador = donation.getDoador().getId().equals(user.getId());
        boolean instituicao = "instituicao".equals(user.getTipo())
                && user.getInstitutionProfile() != null
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
}
