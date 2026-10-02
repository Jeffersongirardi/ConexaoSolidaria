package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.ContactDTO;
import com.conexoessolidarias.api.dto.DonationDTO;
import com.conexoessolidarias.api.dto.InstitutionDTO;
import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.api.dto.OfertaDTO;
import com.conexoessolidarias.api.dto.UserDTO;
import com.conexoessolidarias.model.Donation;
import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.Oferta;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.CampaignRepository;
import com.conexoessolidarias.repository.ContactMessageRepository;
import com.conexoessolidarias.repository.DonationRepository;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.OfertaRepository;
import com.conexoessolidarias.repository.UserRepository;
import com.conexoessolidarias.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminApiController {

    private final UserRepository userRepository;
    private final InstitutionProfileRepository profileRepository;
    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final OfertaRepository ofertaRepository;
    private final NotificationService notificationService;

    public AdminApiController(UserRepository userRepository,
                              InstitutionProfileRepository profileRepository,
                              CampaignRepository campaignRepository,
                              DonationRepository donationRepository,
                              ContactMessageRepository contactMessageRepository,
                              OfertaRepository ofertaRepository,
                              NotificationService notificationService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.campaignRepository = campaignRepository;
        this.donationRepository = donationRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.ofertaRepository = ofertaRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Long>> dashboard() {
        return ResponseEntity.ok(Map.of(
                "usuarios", userRepository.count(),
                "instituicoes", profileRepository.count(),
                "pendentes", profileRepository.countByAprovado(false),
                "doacoes", donationRepository.count(),
                "campanhas", campaignRepository.count(),
                "mensagensNaoLidas", contactMessageRepository.countByLidoFalse()));
    }

    @GetMapping("/institutions")
    public ResponseEntity<List<InstitutionDTO>> institutions(
            @RequestParam(defaultValue = "todas") String filtro) {
        List<InstitutionProfile> lista = switch (filtro) {
            case "pendentes" -> profileRepository.findByAprovadoOrderByDataCadastroDesc(false);
            case "aprovadas" -> profileRepository.findByAprovadoOrderByDataCadastroDesc(true);
            default -> profileRepository.findAllByOrderByDataCadastroDesc();
        };
        return ResponseEntity.ok(lista.stream()
                .map(p -> InstitutionDTO.from(p, false)).toList());
    }

    @PatchMapping("/institutions/{id}/aprovar")
    @Transactional
    public ResponseEntity<InstitutionDTO> aprovar(@PathVariable Long id) {
        InstitutionProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Instituição não encontrada"));
        String cnpj = profile.getCnpj() != null ? profile.getCnpj().replaceAll("\\D", "") : "";
        if (cnpj.length() != 14) {
            throw new IllegalStateException("Aprovação exige CNPJ válido com 14 dígitos (CNPJ ativo obrigatório)");
        }
        profile.setAprovado(true);
        profile.setMotivoRecusa(null);
        return ResponseEntity.ok(InstitutionDTO.from(profileRepository.save(profile), false));
    }

    @PatchMapping("/institutions/{id}/recusar")
    @Transactional
    public ResponseEntity<InstitutionDTO> recusar(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.getOrDefault("motivo", "").trim() : "";
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo da recusa é obrigatório");
        }
        InstitutionProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Instituição não encontrada"));
        profile.setAprovado(false);
        profile.setMotivoRecusa(motivo);
        return ResponseEntity.ok(InstitutionDTO.from(profileRepository.save(profile), false));
    }

    @GetMapping("/doacoes")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DonationDTO>> doacoes() {
        return ResponseEntity.ok(donationRepository.findTop100ByOrderByDataIntencaoDesc()
                .stream().map(DonationDTO::from).toList());
    }

    @PatchMapping("/doacoes/{id}/cancelar")
    @Transactional
    public ResponseEntity<DonationDTO> cancelarDoacao(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.getOrDefault("motivo", "").trim() : "";
        Donation d = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doação não encontrada"));
        if ("cancelado".equals(d.getStatus())) {
            throw new IllegalStateException("Doação já cancelada");
        }
        d.setStatus("cancelado");
        donationRepository.save(d);
        notificationService.notificar(d.getDoador().getId(), "doacao_cancelada_admin",
                "Sua doação de " + d.getQuantidade() + " de " + d.getItem()
                        + " foi cancelada pela moderação"
                        + (!motivo.isBlank() ? ". Motivo: " + motivo : "."),
                "/painel/doador");
        return ResponseEntity.ok(DonationDTO.from(d));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> users() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .map(UserDTO::from).toList());
    }

    @PatchMapping("/users/{id}/toggle")
    @Transactional
    public ResponseEntity<UserDTO> toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        if ("admin".equals(user.getTipo())) {
            throw new IllegalStateException("Não é possível desativar o admin");
        }
        user.setAtivo(!user.getAtivo());
        return ResponseEntity.ok(UserDTO.from(userRepository.save(user)));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ContactDTO>> messages() {
        return ResponseEntity.ok(contactMessageRepository.findAllByOrderByDataEnvioDesc()
                .stream().map(ContactDTO::from).toList());
    }

    @PatchMapping("/messages/{id}/ler")
    @Transactional
    public ResponseEntity<ContactDTO> lerMensagem(@PathVariable Long id) {
        var msg = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mensagem não encontrada"));
        msg.setLido(true);
        return ResponseEntity.ok(ContactDTO.from(contactMessageRepository.save(msg)));
    }

    @DeleteMapping("/messages/{id}")
    @Transactional
    public ResponseEntity<MessageResponse> removerMensagem(@PathVariable Long id) {
        if (!contactMessageRepository.existsById(id)) {
            throw new EntityNotFoundException("Mensagem não encontrada");
        }
        contactMessageRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Mensagem removida"));
    }

    @GetMapping("/ofertas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OfertaDTO>> ofertas(
            @RequestParam(defaultValue = "todas") String filtro) {
        List<Oferta> todas = ofertaRepository.findAllByOrderByDataCriacaoDesc();
        List<Oferta> lista = switch (filtro) {
            case "pendentes" -> todas.stream()
                    .filter(o -> !Boolean.TRUE.equals(o.getAprovado())).toList();
            case "aprovadas" -> todas.stream()
                    .filter(o -> Boolean.TRUE.equals(o.getAprovado())).toList();
            default -> todas;
        };
        return ResponseEntity.ok(lista.stream().map(OfertaDTO::from).toList());
    }

    @PatchMapping("/ofertas/{id}/aprovar")
    @Transactional
    public ResponseEntity<OfertaDTO> aprovarOferta(@PathVariable Long id) {
        Oferta o = ofertaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Oferta não encontrada"));
        o.setAprovado(true);
        o.setMotivoRecusa(null);
        ofertaRepository.save(o);
        notificationService.notificar(o.getDoador().getId(), "oferta_aprovada",
                "Sua oferta \"" + o.getTitulo() + "\" foi aprovada e já está visível às instituições!",
                "/painel/doador");
        return ResponseEntity.ok(OfertaDTO.from(o));
    }

    @PatchMapping("/ofertas/{id}/recusar")
    @Transactional
    public ResponseEntity<OfertaDTO> recusarOferta(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.getOrDefault("motivo", "").trim() : "";
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo da recusa é obrigatório");
        }
        Oferta o = ofertaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Oferta não encontrada"));
        o.setAprovado(false);
        o.setMotivoRecusa(motivo);
        ofertaRepository.save(o);
        notificationService.notificar(o.getDoador().getId(), "oferta_recusada",
                "Sua oferta \"" + o.getTitulo() + "\" não foi aprovada. Motivo: " + motivo,
                "/painel/doador");
        return ResponseEntity.ok(OfertaDTO.from(o));
    }
}
