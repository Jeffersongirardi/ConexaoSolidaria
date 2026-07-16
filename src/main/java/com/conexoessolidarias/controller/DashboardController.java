package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.*;
import com.conexoessolidarias.repository.*;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.FileStorageService;
import com.conexoessolidarias.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final NeedRepository needRepository;
    private final DonationRepository donationRepository;
    private final PaymentRepository paymentRepository;
    private final InstitutionProfileRepository institutionProfileRepository;
    private final NeedImageRepository needImageRepository;
    private final DonationUpdateRepository donationUpdateRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public DashboardController(NeedRepository needRepository,
                               DonationRepository donationRepository,
                               PaymentRepository paymentRepository,
                               InstitutionProfileRepository institutionProfileRepository,
                               NeedImageRepository needImageRepository,
                               DonationUpdateRepository donationUpdateRepository,
                               NotificationService notificationService,
                               FileStorageService fileStorageService,
                               UserRepository userRepository) {
        this.needRepository = needRepository;
        this.donationRepository = donationRepository;
        this.paymentRepository = paymentRepository;
        this.institutionProfileRepository = institutionProfileRepository;
        this.needImageRepository = needImageRepository;
        this.donationUpdateRepository = donationUpdateRepository;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return principal != null ? principal.getUser() : null;
    }

    @GetMapping("/doador")
    public String doador(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null || !"doador".equals(user.getTipo())) return "redirect:/";

        List<Donation> doacoes = donationRepository.findByDoadorIdOrderByDataIntencaoDesc(user.getId());
        List<Payment> pagamentos = paymentRepository.findByDoadorIdOrderByDataCriacaoDesc(user.getId());

        int total = doacoes.size() + pagamentos.size();
        long recebidas = doacoes.stream().filter(d -> "recebido".equals(d.getStatus())).count()
                       + pagamentos.stream().filter(p -> "confirmado".equals(p.getStatus())).count();
        long pendentes = doacoes.stream().filter(d -> "pendente".equals(d.getStatus())).count()
                       + pagamentos.stream().filter(p -> "pendente".equals(p.getStatus())).count();

        model.addAttribute("doacoes", doacoes);
        model.addAttribute("pagamentos", pagamentos);
        model.addAttribute("stats", Map.of("total", total, "recebidas", recebidas, "pendentes", pendentes));

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalConfirmadas",
            doacoes.stream().filter(d -> "recebido".equals(d.getStatus())).count()
          + pagamentos.stream().filter(p -> "confirmado".equals(p.getStatus())).count());
        analytics.put("totalValorDoado",
            pagamentos.stream().filter(p -> "confirmado".equals(p.getStatus()))
                .map(Payment::getValor).reduce(BigDecimal.ZERO, BigDecimal::add));
        Map<String, Long> doacoesPorCategoria = new HashMap<>();
        for (Donation d : doacoes) {
            doacoesPorCategoria.merge(d.getCategoria() != null ? d.getCategoria() : "outro", 1L, Long::sum);
        }
        analytics.put("doacoesPorCategoria", doacoesPorCategoria);
        model.addAttribute("analytics", analytics);
        return "dashboard-doador";
    }

    @GetMapping("/instituicao")
    public String instituicao(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null || !"instituicao".equals(user.getTipo())) return "redirect:/";

        InstitutionProfile profile = user.getInstitutionProfile();
        if (profile == null || !Boolean.TRUE.equals(profile.getAprovado())) {
            model.addAttribute("pendente", true);
            model.addAttribute("profile", profile);
            return "dashboard-instituicao";
        }

        List<Need> needs = needRepository.findByInstitutionIdOrderByDataCriacaoDesc(profile.getId());
        List<Long> needIds = needs.stream().map(Need::getId).toList();
        List<Donation> doacoes = needIds.isEmpty() ? Collections.emptyList()
                : donationRepository.findByNeedIdInOrderByDataIntencaoDesc(needIds);
        List<Payment> pagamentos = paymentRepository.findByInstituicaoIdOrderByDataCriacaoDesc(profile.getId());

        long totalDoacoes = doacoes.size() + pagamentos.size();
        long recebidas = doacoes.stream().filter(d -> "recebido".equals(d.getStatus())).count()
                       + pagamentos.stream().filter(p -> "confirmado".equals(p.getStatus())).count();
        long pendentes = doacoes.stream().filter(d -> "pendente".equals(d.getStatus())).count()
                       + pagamentos.stream().filter(p -> "pendente".equals(p.getStatus())).count();
        long ativas = needs.stream().filter(Need::getAtivo).count();

        model.addAttribute("currentUser", user);
        model.addAttribute("needs", needs);
        model.addAttribute("doacoes", doacoes);
        model.addAttribute("pagamentos", pagamentos);
        model.addAttribute("stats", Map.of(
            "total_doacoes", totalDoacoes, "recebidas", recebidas,
            "pendentes", pendentes, "necessidades_ativas", ativas
        ));

        long totalConfirmadas = doacoes.stream().filter(d -> "recebido".equals(d.getStatus())).count()
                              + pagamentos.stream().filter(p -> "confirmado".equals(p.getStatus())).count();
        Map<String, Long> doacoesPorCategoria = new HashMap<>();
        for (Donation d : doacoes) {
            String cat = d.getCategoria() != null ? d.getCategoria() : "outro";
            doacoesPorCategoria.merge(cat, 1L, Long::sum);
        }
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalConfirmadas", totalConfirmadas);
        analytics.put("doacoesPorCategoria", doacoesPorCategoria);
        BigDecimal totalValor = pagamentos.stream()
            .filter(p -> "confirmado".equals(p.getStatus()))
            .map(Payment::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.put("totalValorRecebido", totalValor);
        model.addAttribute("analytics", analytics);
        return "dashboard-instituicao";
    }

    @PostMapping("/need/criar")
    public String criarNeed(@AuthenticationPrincipal CustomUserDetails principal,
                            @RequestParam String titulo,
                            @RequestParam String descricao,
                            @RequestParam(defaultValue = "outro") String categoria,
                            @RequestParam String quantidade,
                            @RequestParam(defaultValue = "media") String urgencia,
                            @RequestParam(defaultValue = "false") boolean aceitaFinanceiro,
                            @RequestParam("imagens") MultipartFile[] imagens,
                            RedirectAttributes redirect) {
        User user = getCurrentUser(principal);
        if (user == null || !"instituicao".equals(user.getTipo())) return "redirect:/";

        Need need = new Need();
        need.setInstitution(user.getInstitutionProfile());
        need.setTitulo(titulo);
        need.setDescricao(descricao);
        need.setCategoria(categoria);
        need.setQuantidadeAlvo(quantidade);
        need.setUrgencia(urgencia);
        need.setAceitaFinanceiro(aceitaFinanceiro);
        need = needRepository.save(need);

        for (MultipartFile file : imagens) {
            if (file != null && !file.isEmpty()) {
                String url = fileStorageService.save(file, "needs");
                if (url != null) {
                    NeedImage img = new NeedImage(need, url);
                    needImageRepository.save(img);
                }
            }
        }

        redirect.addFlashAttribute("success", "Necessidade criada com sucesso!");
        return "redirect:/dashboard/instituicao";
    }

    @GetMapping("/need/{id}/editar")
    public String editarNeedForm(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails principal,
                                 Model model) {
        Need need = needRepository.findById(id).orElse(null);
        if (need == null || !pertenceInstituicao(need, principal)) return "redirect:/dashboard/instituicao";
        model.addAttribute("need", need);
        return "need-editar";
    }

    @PostMapping("/need/{id}/editar")
    public String editarNeed(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails principal,
                             @RequestParam String titulo,
                             @RequestParam String descricao,
                             @RequestParam(defaultValue = "outro") String categoria,
                             @RequestParam String quantidade,
                             @RequestParam(defaultValue = "media") String urgencia,
                             @RequestParam(defaultValue = "false") boolean aceitaFinanceiro,
                             @RequestParam("imagens") MultipartFile[] imagens,
                             RedirectAttributes redirect) {
        Need need = needRepository.findById(id).orElse(null);
        if (need == null || !pertenceInstituicao(need, principal)) return "redirect:/dashboard/instituicao";

        need.setTitulo(titulo);
        need.setDescricao(descricao);
        need.setCategoria(categoria);
        need.setQuantidadeAlvo(quantidade);
        need.setUrgencia(urgencia);
        need.setAceitaFinanceiro(aceitaFinanceiro);
        needRepository.save(need);

        for (MultipartFile file : imagens) {
            if (file != null && !file.isEmpty()) {
                String url = fileStorageService.save(file, "needs");
                if (url != null) {
                    int maxOrdem = need.getImages().stream().mapToInt(NeedImage::getOrdem).max().orElse(0);
                    NeedImage img = new NeedImage(need, url);
                    img.setOrdem(maxOrdem + 1);
                    needImageRepository.save(img);
                }
            }
        }

        redirect.addFlashAttribute("success", "Necessidade atualizada!");
        return "redirect:/dashboard/instituicao";
    }

    @GetMapping("/need/{id}/toggle")
    public String toggleNeed(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails principal) {
        Need need = needRepository.findById(id).orElse(null);
        if (need != null && pertenceInstituicao(need, principal)) {
            need.setAtivo(!need.getAtivo());
            need.setDataEncerramento(need.getAtivo() ? null : LocalDateTime.now());
            needRepository.save(need);
        }
        return "redirect:/dashboard/instituicao";
    }

    @PostMapping("/need/imagem/{imgId}/remover")
    public String removerImagem(@PathVariable Long imgId,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        NeedImage img = needImageRepository.findById(imgId).orElse(null);
        if (img != null) {
            Need need = img.getNeed();
            if (pertenceInstituicao(need, principal)) {
                needImageRepository.delete(img);
            }
        }
        return "redirect:/dashboard/instituicao";
    }

    @PostMapping("/doar/{needId}")
    public String doar(@PathVariable Long needId,
                       @AuthenticationPrincipal CustomUserDetails principal,
                       @RequestParam String item,
                       @RequestParam String quantidade,
                       @RequestParam(defaultValue = "outro") String categoria,
                       @RequestParam(required = false) String observacao,
                       RedirectAttributes redirect) {
        User user = getCurrentUser(principal);
        if (user == null || !"doador".equals(user.getTipo())) return "redirect:/";

        Need need = needRepository.findById(needId).orElse(null);
        if (need == null || !need.getAtivo()) {
            redirect.addFlashAttribute("error", "Necessidade não encontrada ou inativa.");
            return "redirect:/necessidades";
        }

        Donation donation = new Donation();
        donation.setDoador(user);
        donation.setNeed(need);
        donation.setItem(item);
        donation.setQuantidade(quantidade);
        donation.setCategoria(categoria);
        donation.setObservacao(observacao);
        donationRepository.save(donation);

        notificationService.notificar(
            need.getInstitution().getUser().getId(),
            "nova_doacao",
            user.getNome() + " quer doar " + quantidade + " de " + item + " para \"" + need.getTitulo() + "\"",
            "/dashboard/instituicao"
        );

        redirect.addFlashAttribute("success", "Intenção de doação registrada!");
        return "redirect:/dashboard/doador";
    }

    @PostMapping("/doar-financeiro/{needId}")
    public String doarFinanceiro(@PathVariable Long needId,
                                 @AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestParam BigDecimal valor,
                                 @RequestParam(defaultValue = "pix") String metodo,
                                 RedirectAttributes redirect) {
        User user = getCurrentUser(principal);
        if (user == null || !"doador".equals(user.getTipo())) return "redirect:/";

        Need need = needRepository.findById(needId).orElse(null);
        if (need == null || !need.getAtivo()) {
            redirect.addFlashAttribute("error", "Necessidade inativa.");
            return "redirect:/necessidades";
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            redirect.addFlashAttribute("error", "Valor inválido.");
            return "redirect:/necessidades/" + needId;
        }

        Payment payment = new Payment();
        payment.setDoador(user);
        payment.setInstituicao(need.getInstitution());
        payment.setNecessidade(need);
        payment.setValor(valor);
        payment.setMetodo(metodo);
        payment = paymentRepository.save(payment);

        return "redirect:/pagamento/" + payment.getUuid();
    }

    @GetMapping("/confirmar/{donationId}")
    public String confirmarRecebimento(@PathVariable Long donationId,
                                        @AuthenticationPrincipal CustomUserDetails principal) {
        Donation donation = donationRepository.findById(donationId).orElse(null);
        if (donation == null) return "redirect:/dashboard/instituicao";

        User user = getCurrentUser(principal);
        if (user == null || !"instituicao".equals(user.getTipo())) return "redirect:/";

        Need need = donation.getNeed();
        InstitutionProfile profile = user.getInstitutionProfile();
        if (!need.getInstitution().getId().equals(profile.getId())) return "redirect:/dashboard/instituicao";

        donation.setStatus("recebido");
        donation.setDataRecebimento(LocalDateTime.now());
        donationRepository.save(donation);

        need.setProgresso(Math.min(100, (need.getProgresso() != null ? need.getProgresso() : 0) + 10));
        needRepository.save(need);

        notificationService.notificar(
            donation.getDoador().getId(),
            "doacao_confirmada",
            "Sua doação de " + donation.getQuantidade() + " de " + donation.getItem()
                + " foi confirmada como recebida por " + profile.getRazaoSocial() + "!",
            "/dashboard/doador"
        );

        return "redirect:/dashboard/instituicao";
    }

    @GetMapping("/cancelar/{donationId}")
    public String cancelarDoacao(@PathVariable Long donationId,
                                  @AuthenticationPrincipal CustomUserDetails principal) {
        Donation donation = donationRepository.findById(donationId).orElse(null);
        if (donation == null) return "redirect:/dashboard/doador";

        User user = getCurrentUser(principal);
        if (user == null || !donation.getDoador().getId().equals(user.getId())) return "redirect:/dashboard/doador";
        if (!"pendente".equals(donation.getStatus())) return "redirect:/dashboard/doador";

        donation.setStatus("cancelado");
        donationRepository.save(donation);
        return "redirect:/dashboard/doador";
    }

    @PostMapping("/doacao/{donationId}/atualizar")
    public String atualizarDoacao(@PathVariable Long donationId,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestParam String mensagem,
                                  @RequestParam("foto") MultipartFile foto) {
        Donation donation = donationRepository.findById(donationId).orElse(null);
        if (donation == null) return "redirect:/dashboard/instituicao";

        User user = getCurrentUser(principal);
        if (user == null || !"instituicao".equals(user.getTipo())) return "redirect:/";

        Need need = donation.getNeed();
        if (!need.getInstitution().getUser().getId().equals(user.getId())) return "redirect:/dashboard/instituicao";

        DonationUpdate update = new DonationUpdate(donation, mensagem);
        if (foto != null && !foto.isEmpty()) {
            String url = fileStorageService.save(foto, "updates");
            update.setFotoUrl(url);
        }
        donationUpdateRepository.save(update);

        return "redirect:/dashboard/instituicao";
    }

    @GetMapping("/comprovante/{donationId}")
    public String comprovanteDoacao(@PathVariable Long donationId,
                                    @AuthenticationPrincipal CustomUserDetails principal,
                                    Model model) {
        Donation donation = donationRepository.findById(donationId).orElse(null);
        if (donation == null || !donation.getDoador().getId().equals(principal.getUser().getId())) {
            return "redirect:/dashboard/doador";
        }
        model.addAttribute("tipo", "fisico");
        model.addAttribute("item", donation.getItem());
        model.addAttribute("quantidade", donation.getQuantidade());
        model.addAttribute("instituicao", donation.getNeed().getInstitution().getRazaoSocial());
        model.addAttribute("data", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(donation.getDataIntencao()));
        model.addAttribute("status", donation.getStatus());
        model.addAttribute("observacao", donation.getObservacao());
        return "comprovante";
    }

    private boolean pertenceInstituicao(Need need, CustomUserDetails principal) {
        if (principal == null) return false;
        User user = principal.getUser();
        InstitutionProfile profile = user.getInstitutionProfile();
        return profile != null && need.getInstitution().getId().equals(profile.getId());
    }
}
