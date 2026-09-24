package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.InstitutionDTO;
import com.conexoessolidarias.api.dto.InstitutionProfileRequest;
import com.conexoessolidarias.model.Campaign;
import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.CampaignRepository;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.StorageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/institutions")
public class InstitutionApiController {

    private final InstitutionProfileRepository profileRepository;
    private final CampaignRepository campaignRepository;
    private final StorageService storageService;

    public InstitutionApiController(InstitutionProfileRepository profileRepository,
                                    CampaignRepository campaignRepository,
                                    StorageService storageService) {
        this.profileRepository = profileRepository;
        this.campaignRepository = campaignRepository;
        this.storageService = storageService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<InstitutionDTO>> listar() {
        return ResponseEntity.ok(profileRepository.findByAprovadoOrderByDataCadastroDesc(true)
                .stream().map(p -> InstitutionDTO.from(p, false)).toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<InstitutionDTO> detalhe(@PathVariable Long id) {
        InstitutionProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Instituição não encontrada"));
        if (!Boolean.TRUE.equals(profile.getAprovado())) {
            throw new EntityNotFoundException("Instituição não encontrada");
        }
        List<Campaign> ativas = campaignRepository
                .findByInstitutionIdOrderByDataCriacaoDesc(profile.getId())
                .stream().filter(Campaign::getAtivo).toList();
        profile.setCampaigns(ativas);
        return ResponseEntity.ok(InstitutionDTO.from(profile, true));
    }

    @GetMapping("/minha")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional(readOnly = true)
    public ResponseEntity<InstitutionDTO> minha(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(InstitutionDTO.from(requireProfile(principal.getUser()), false));
    }

    @PutMapping("/minha")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<InstitutionDTO> atualizar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody InstitutionProfileRequest req) {
        InstitutionProfile profile = requireProfile(principal.getUser());
        profile.setNomeFantasia(req.nomeFantasia());
        profile.setEndereco(req.endereco());
        profile.setWebsite(req.website());
        profile.setDescricao(req.descricao());
        profile.setCategoriaAtuacao(req.categoriaAtuacao());
        profile.setWhatsapp(req.whatsapp());
        profile.setPixKey(req.pixKey());
        profile.setPixTitular(req.pixTitular());
        User user = principal.getUser();
        if (req.whatsapp() != null) user.setWhatsapp(req.whatsapp());
        return ResponseEntity.ok(InstitutionDTO.from(profileRepository.save(profile), false));
    }

    @PostMapping(value = "/minha/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<InstitutionDTO> foto(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam("foto") MultipartFile foto) {
        InstitutionProfile profile = requireProfile(principal.getUser());
        String url = storageService.save(foto, "avatars");
        if (url == null) {
            throw new IllegalArgumentException("Imagem inválida (png/jpg/gif/webp até 5MB)");
        }
        profile.setFotoUrl(url);
        return ResponseEntity.ok(InstitutionDTO.from(profileRepository.save(profile), false));
    }

    private InstitutionProfile requireProfile(User user) {
        InstitutionProfile profile = user.getInstitutionProfile();
        if (profile == null) throw new EntityNotFoundException("Perfil de instituição não encontrado");
        return profile;
    }
}
