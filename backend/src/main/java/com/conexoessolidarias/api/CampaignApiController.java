package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.CampaignDTO;
import com.conexoessolidarias.api.dto.CampaignRequest;
import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.model.Campaign;
import com.conexoessolidarias.model.CampaignImage;
import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.CampaignImageRepository;
import com.conexoessolidarias.repository.CampaignRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.StorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/campaigns")
public class CampaignApiController {

    private final CampaignRepository campaignRepository;
    private final CampaignImageRepository imageRepository;
    private final StorageService storageService;

    public CampaignApiController(CampaignRepository campaignRepository,
                                 CampaignImageRepository imageRepository,
                                 StorageService storageService) {
        this.campaignRepository = campaignRepository;
        this.imageRepository = imageRepository;
        this.storageService = storageService;
    }

    @GetMapping
    public Page<CampaignDTO> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String urgencia,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        if ("todas".equals(categoria)) categoria = null;
        if ("todas".equals(urgencia)) urgencia = null;
        if (busca != null && busca.isBlank()) busca = null;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        return campaignRepository.filtrar(categoria, urgencia, busca, pageable)
                .map(CampaignDTO::from);
    }

    @GetMapping("/destaques")
    public ResponseEntity<java.util.List<CampaignDTO>> destaques() {
        return ResponseEntity.ok(campaignRepository.findTop6ByAtivoTrueOrderByDataCriacaoDesc()
                .stream().map(CampaignDTO::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignDTO> detalhe(@PathVariable Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Campanha não encontrada"));
        return ResponseEntity.ok(CampaignDTO.from(campaign));
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('INSTITUICAO')")
    public ResponseEntity<java.util.List<CampaignDTO>> minhas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        InstitutionProfile profile = requireProfile(principal.getUser());
        return ResponseEntity.ok(campaignRepository
                .findByInstitutionIdOrderByDataCriacaoDesc(profile.getId())
                .stream().map(CampaignDTO::from).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<CampaignDTO> criar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CampaignRequest req) {
        InstitutionProfile profile = requireApprovedProfile(principal.getUser());
        Campaign campaign = new Campaign();
        apply(campaign, req);
        campaign.setInstitution(profile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CampaignDTO.from(campaignRepository.save(campaign)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<CampaignDTO> atualizar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CampaignRequest req) {
        Campaign campaign = requireOwner(id, principal.getUser());
        apply(campaign, req);
        return ResponseEntity.ok(CampaignDTO.from(campaignRepository.save(campaign)));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<CampaignDTO> toggle(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Campaign campaign = requireOwner(id, principal.getUser());
        campaign.setAtivo(!campaign.getAtivo());
        campaign.setDataEncerramento(campaign.getAtivo() ? null : LocalDateTime.now());
        return ResponseEntity.ok(CampaignDTO.from(campaignRepository.save(campaign)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<MessageResponse> remover(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Campaign campaign = requireOwner(id, principal.getUser());
        campaignRepository.delete(campaign);
        return ResponseEntity.ok(new MessageResponse("Campanha removida"));
    }

    @PostMapping(value = "/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<CampaignDTO> adicionarImagem(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam("imagem") MultipartFile imagem) {
        Campaign campaign = requireOwner(id, principal.getUser());
        String url = storageService.save(imagem, "campaigns");
        if (url == null) {
            throw new IllegalArgumentException("Imagem inválida (png/jpg/gif/webp até 5MB)");
        }
        int maxOrdem = campaign.getImages().stream()
                .mapToInt(i -> i.getOrdem() != null ? i.getOrdem() : 0).max().orElse(0);
        CampaignImage img = new CampaignImage(campaign, url);
        img.setOrdem(maxOrdem + 1);
        imageRepository.save(img);
        campaign.getImages().add(img);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CampaignDTO.from(campaignRepository.findById(id).orElseThrow()));
    }

    @DeleteMapping("/imagens/{imgId}")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<MessageResponse> removerImagem(
            @PathVariable Long imgId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CampaignImage img = imageRepository.findById(imgId)
                .orElseThrow(() -> new EntityNotFoundException("Imagem não encontrada"));
        requireOwner(img.getCampaign().getId(), principal.getUser());
        imageRepository.delete(img);
        return ResponseEntity.ok(new MessageResponse("Imagem removida"));
    }

    private void apply(Campaign campaign, CampaignRequest req) {
        campaign.setTitulo(req.titulo());
        campaign.setDescricao(req.descricao());
        campaign.setCategoria(req.categoria() != null ? req.categoria() : "outro");
        campaign.setQuantidadeAlvo(req.quantidadeAlvo());
        campaign.setUrgencia(req.urgencia() != null ? req.urgencia() : "media");
        if (req.aceitaFinanceiro() != null) campaign.setAceitaFinanceiro(req.aceitaFinanceiro());
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

    private Campaign requireOwner(Long id, User user) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Campanha não encontrada"));
        InstitutionProfile profile = requireProfile(user);
        if (!campaign.getInstitution().getId().equals(profile.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        return campaign;
    }
}
