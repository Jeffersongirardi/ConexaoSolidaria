package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.api.dto.OfertaDTO;
import com.conexoessolidarias.api.dto.OfertaRequest;
import com.conexoessolidarias.model.Categoria;
import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.Oferta;
import com.conexoessolidarias.model.OfertaImage;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.OfertaImageRepository;
import com.conexoessolidarias.repository.OfertaRepository;
import com.conexoessolidarias.security.CustomUserDetails;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/ofertas")
public class OfertaApiController {

    /** Prazo para a instituição coletar/receber após reivindicar. */
    public static final int PRAZO_COLETA_DIAS = 7;

    private static final Set<String> ESTADOS = Set.of("novo", "usado", "bom_estado");

    private final OfertaRepository ofertaRepository;
    private final OfertaImageRepository imageRepository;
    private final NotificationService notificationService;
    private final StorageService storageService;

    public OfertaApiController(OfertaRepository ofertaRepository,
                               OfertaImageRepository imageRepository,
                               NotificationService notificationService,
                               StorageService storageService) {
        this.ofertaRepository = ofertaRepository;
        this.imageRepository = imageRepository;
        this.notificationService = notificationService;
        this.storageService = storageService;
    }

    @GetMapping("/contagem")
    public ResponseEntity<java.util.Map<String, Long>> contagem() {
        long total = ofertaRepository.findByStatusOrderByDataCriacaoDesc(Oferta.DISPONIVEL)
                .stream()
                .filter(o -> Boolean.TRUE.equals(o.getAprovado()))
                .filter(o -> o.getDisponivelAte() == null || !o.getDisponivelAte().isBefore(LocalDate.now()))
                .count();
        return ResponseEntity.ok(java.util.Map.of("total", total));
    }

    @GetMapping
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OfertaDTO>> disponiveis(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String busca) {
        requireApprovedProfile(principal.getUser());
        final String cat = "todas".equals(categoria) ? null : categoria;
        final String cid = cidade;
        final String q = busca;
        List<Oferta> lista = ofertaRepository.findByStatusOrderByDataCriacaoDesc(Oferta.DISPONIVEL)
                .stream()
                .filter(o -> Boolean.TRUE.equals(o.getAprovado()))
                .filter(o -> o.getDisponivelAte() == null || !o.getDisponivelAte().isBefore(LocalDate.now()))
                .filter(o -> cat == null || cat.equals(o.getCategoria()))
                .filter(o -> cid == null || cid.isBlank()
                        || (o.getCidade() != null && o.getCidade().toLowerCase().contains(cid.toLowerCase())))
                .filter(o -> q == null || q.isBlank()
                        || ((o.getTitulo() + " " + o.getDescricao()).toLowerCase().contains(q.toLowerCase())))
                .toList();
        return ResponseEntity.ok(lista.stream().map(OfertaDTO::from).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOADOR', 'INSTITUICAO', 'ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<OfertaDTO> detalhe(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Oferta o = require(id);
        User user = principal.getUser();
        if ("admin".equals(user.getTipo())) return ResponseEntity.ok(OfertaDTO.from(o));
        if ("doador".equals(user.getTipo())) {
            if (!o.getDoador().getId().equals(user.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
            }
            return ResponseEntity.ok(OfertaDTO.from(o));
        }
        requireApprovedProfile(user);
        if (!Boolean.TRUE.equals(o.getAprovado())) {
            throw new EntityNotFoundException("Oferta não encontrada");
        }
        return ResponseEntity.ok(OfertaDTO.from(o));
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OfertaDTO>> minhas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ofertaRepository
                .findByDoadorIdOrderByDataCriacaoDesc(principal.getUser().getId())
                .stream().map(OfertaDTO::from).toList());
    }

    @GetMapping("/reservadas")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OfertaDTO>> reservadas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        InstitutionProfile profile = requireProfile(principal.getUser());
        return ResponseEntity.ok(ofertaRepository
                .findByInstituicaoIdAndStatusOrderByReservadaEmDesc(profile.getId(), Oferta.RESERVADA)
                .stream().map(OfertaDTO::from).toList());
    }

    @GetMapping("/recebidas")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OfertaDTO>> recebidas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        InstitutionProfile profile = requireProfile(principal.getUser());
        return ResponseEntity.ok(ofertaRepository
                .findByInstituicaoIdAndStatusOrderByReservadaEmDesc(profile.getId(), Oferta.ENTREGUE)
                .stream().map(OfertaDTO::from).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<OfertaDTO> criar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody OfertaRequest req) {
        Oferta o = new Oferta();
        o.setDoador(principal.getUser());
        apply(o, req);
        o.setStatus(Oferta.DISPONIVEL);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OfertaDTO.from(ofertaRepository.save(o)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<OfertaDTO> atualizar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody OfertaRequest req) {
        Oferta o = requireDono(id, principal.getUser());
        if (!Oferta.DISPONIVEL.equals(o.getStatus())) {
            throw new IllegalStateException("Só ofertas disponíveis podem ser editadas");
        }
        apply(o, req);
        return ResponseEntity.ok(OfertaDTO.from(ofertaRepository.save(o)));
    }

    public static final Set<String> MOTIVOS_CANCELAMENTO = Set.of(
            "doado_outro_meio", "indisponivel", "erro_anuncio", "desistencia", "outro");

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<OfertaDTO> cancelarComMotivo(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody Map<String, String> body) {
        String motivo = body != null ? body.getOrDefault("motivo", "").trim() : "";
        String detalhe = body != null ? body.getOrDefault("detalhe", "").trim() : "";
        if (!MOTIVOS_CANCELAMENTO.contains(motivo)) {
            throw new IllegalArgumentException("Motivo do cancelamento é obrigatório");
        }
        if ("outro".equals(motivo) && detalhe.isBlank()) {
            throw new IllegalStateException("Descreva o motivo do cancelamento");
        }
        Oferta o = requireDono(id, principal.getUser());
        if (Oferta.ENTREGUE.equals(o.getStatus())) {
            throw new IllegalStateException("Oferta já entregue não pode ser cancelada");
        }
        if (Oferta.CANCELADA.equals(o.getStatus())) {
            throw new IllegalStateException("Oferta já cancelada");
        }
        InstitutionProfile reservante = Oferta.RESERVADA.equals(o.getStatus()) ? o.getInstituicao() : null;
        o.setStatus(Oferta.CANCELADA);
        o.setMotivoRecusa(null);
        ofertaRepository.save(o);
        if (reservante != null) {
            notificationService.notificar(reservante.getUser().getId(), "oferta_cancelada",
                    "A oferta \"" + o.getTitulo() + "\" foi cancelada pelo doador.",
                    "/painel/instituicao");
        }
        return ResponseEntity.ok(OfertaDTO.from(o));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<MessageResponse> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Oferta o = requireDono(id, principal.getUser());
        if (Oferta.ENTREGUE.equals(o.getStatus())) {
            throw new IllegalStateException("Oferta já entregue não pode ser cancelada");
        }
        o.setStatus(Oferta.CANCELADA);
        ofertaRepository.save(o);
        return ResponseEntity.ok(new MessageResponse("Oferta cancelada"));
    }

    @PostMapping("/{id}/reivindicar")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<OfertaDTO> reivindicar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        InstitutionProfile profile = requireApprovedProfile(principal.getUser());
        Oferta o = require(id);
        if (!Boolean.TRUE.equals(o.getAprovado())) {
            throw new IllegalStateException("Oferta ainda não aprovada");
        }
        if (!Oferta.DISPONIVEL.equals(o.getStatus())) {
            throw new IllegalStateException("Oferta não está mais disponível");
        }
        if (o.getDisponivelAte() != null && o.getDisponivelAte().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Prazo de disponibilidade expirado");
        }
        o.setInstituicao(profile);
        o.setStatus(Oferta.RESERVADA);
        o.setReservadaEm(LocalDateTime.now());
        o.setPrazoColeta(LocalDate.now().plusDays(PRAZO_COLETA_DIAS));
        ofertaRepository.save(o);
        notificationService.notificar(o.getDoador().getId(), "oferta_reservada",
                profile.getRazaoSocial() + " reservou sua oferta \"" + o.getTitulo()
                        + "\" e tem até " + o.getPrazoColeta() + " para coletar.",
                "/painel/doador");
        return ResponseEntity.ok(OfertaDTO.from(o));
    }

    @PostMapping("/{id}/confirmar-recebimento")
    @PreAuthorize("hasRole('INSTITUICAO')")
    @Transactional
    public ResponseEntity<OfertaDTO> confirmarRecebimento(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        InstitutionProfile profile = requireProfile(principal.getUser());
        Oferta o = require(id);
        if (!Oferta.RESERVADA.equals(o.getStatus())
                || o.getInstituicao() == null
                || !o.getInstituicao().getId().equals(profile.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        o.setStatus(Oferta.ENTREGUE);
        o.setDataEntrega(LocalDateTime.now());
        ofertaRepository.save(o);
        notificationService.notificar(o.getDoador().getId(), "oferta_entregue",
                profile.getRazaoSocial() + " confirmou o recebimento de \"" + o.getTitulo() + "\". Obrigado!",
                "/painel/doador");
        return ResponseEntity.ok(OfertaDTO.from(o));
    }

    @PostMapping("/{id}/liberar")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<OfertaDTO> liberar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Oferta o = requireDono(id, principal.getUser());
        if (!Oferta.RESERVADA.equals(o.getStatus())) {
            throw new IllegalStateException("Só ofertas reservadas voltam a ficar disponíveis");
        }
        InstitutionProfile anterior = o.getInstituicao();
        o.setInstituicao(null);
        o.setStatus(Oferta.DISPONIVEL);
        o.setReservadaEm(null);
        o.setPrazoColeta(null);
        ofertaRepository.save(o);
        if (anterior != null) {
            notificationService.notificar(anterior.getUser().getId(), "oferta_liberada",
                    "A oferta \"" + o.getTitulo() + "\" voltou a ficar disponível.",
                    "/painel/instituicao");
        }
        return ResponseEntity.ok(OfertaDTO.from(o));
    }

    @PostMapping(value = "/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<OfertaDTO> adicionarImagem(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam("imagem") MultipartFile imagem) {
        Oferta o = requireDono(id, principal.getUser());
        String url = storageService.save(imagem, "ofertas");
        if (url == null) {
            throw new IllegalArgumentException("Imagem inválida (png/jpg/gif/webp até 5MB)");
        }
        int maxOrdem = o.getImages().stream()
                .mapToInt(i -> i.getOrdem() != null ? i.getOrdem() : 0).max().orElse(0);
        OfertaImage img = new OfertaImage(o, url);
        img.setOrdem(maxOrdem + 1);
        imageRepository.save(img);
        o.getImages().add(img);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OfertaDTO.from(ofertaRepository.findById(id).orElseThrow()));
    }

    @DeleteMapping("/imagens/{imgId}")
    @PreAuthorize("hasRole('DOADOR')")
    @Transactional
    public ResponseEntity<MessageResponse> removerImagem(
            @PathVariable Long imgId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        OfertaImage img = imageRepository.findById(imgId)
                .orElseThrow(() -> new EntityNotFoundException("Imagem não encontrada"));
        requireDono(img.getOferta().getId(), principal.getUser());
        imageRepository.delete(img);
        return ResponseEntity.ok(new MessageResponse("Imagem removida"));
    }

    private void apply(Oferta o, OfertaRequest req) {
        o.setTitulo(req.titulo());
        o.setDescricao(req.descricao());
        o.setCategoria(Categoria.normalizar(req.categoria()));
        o.setEstadoItem(req.estadoItem() != null && ESTADOS.contains(req.estadoItem())
                ? req.estadoItem() : "usado");
        o.setCidade(req.cidade());
        o.setPrecisaColeta(Boolean.TRUE.equals(req.precisaColeta()));
        o.setEnderecoColeta(req.enderecoColeta());
        o.setDisponivelAte(req.disponivelAte());
    }

    private Oferta require(Long id) {
        return ofertaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Oferta não encontrada"));
    }

    private Oferta requireDono(Long id, User user) {
        Oferta o = require(id);
        if (!o.getDoador().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        return o;
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
