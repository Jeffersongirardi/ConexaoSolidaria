package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.Payment;
import com.conexoessolidarias.repository.PaymentRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.FileStorageService;
import com.conexoessolidarias.service.NotificationService;
import com.conexoessolidarias.service.QrCodeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/pagamento")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final QrCodeService qrCodeService;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    public PaymentController(PaymentRepository paymentRepository,
                             QrCodeService qrCodeService,
                             NotificationService notificationService,
                             FileStorageService fileStorageService) {
        this.paymentRepository = paymentRepository;
        this.qrCodeService = qrCodeService;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{uuid}")
    public String pagamento(@PathVariable String uuid,
                            @AuthenticationPrincipal CustomUserDetails principal,
                            Model model) {
        Payment payment = paymentRepository.findByUuid(uuid).orElse(null);
        if (payment == null || !payment.getDoador().getId().equals(principal.getUser().getId())) {
            return "redirect:/dashboard/doador";
        }
        if (!"pendente".equals(payment.getStatus())) {
            return "redirect:/dashboard/doador";
        }

        if ("pix".equals(payment.getMetodo())) {
            String pixKey = payment.getInstituicao().getPixKey();
            if (pixKey == null) pixKey = "chave@exemplo.org";
            String qrcode = qrCodeService.gerarQrCodeBase64("pix://" + pixKey + "?amount=" + payment.getValor(), 250, 250);
            model.addAttribute("pixKey", pixKey);
            model.addAttribute("pixQrcode", qrcode);
            return "pagamento-pix";
        }

        if ("cartao".equals(payment.getMetodo())) {
            return "pagamento-cartao";
        }

        if ("transferencia".equals(payment.getMetodo())) {
            return "pagamento-transferencia";
        }

        return "pagamento-sucesso";
    }

    @PostMapping("/{uuid}/confirmar-pix")
    public String confirmarPix(@PathVariable String uuid,
                               @AuthenticationPrincipal CustomUserDetails principal) {
        return processarPagamento(uuid, principal);
    }

    @PostMapping("/{uuid}/confirmar-cartao")
    public String confirmarCartao(@PathVariable String uuid,
                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return processarPagamento(uuid, principal);
    }

    @PostMapping("/{uuid}/confirmar-transferencia")
    public String confirmarTransferencia(@PathVariable String uuid,
                                         @AuthenticationPrincipal CustomUserDetails principal,
                                         @RequestParam("comprovante") MultipartFile comprovante) {
        Payment payment = paymentRepository.findByUuid(uuid).orElse(null);
        if (payment == null || !payment.getDoador().getId().equals(principal.getUser().getId())) {
            return "redirect:/dashboard/doador";
        }
        if (!"pendente".equals(payment.getStatus())) {
            return "redirect:/dashboard/doador";
        }

        if (comprovante != null && !comprovante.isEmpty()) {
            String url = fileStorageService.save(comprovante, "comprovantes");
            if (url != null) payment.setComprovanteUrl(url);
        }

        return processarPagamento(uuid, principal);
    }

    private String processarPagamento(String uuid, CustomUserDetails principal) {
        Payment payment = paymentRepository.findByUuid(uuid).orElse(null);
        if (payment == null || !payment.getDoador().getId().equals(principal.getUser().getId())) {
            return "redirect:/dashboard/doador";
        }

        payment.setStatus("confirmado");
        payment.setTransacaoId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        payment.setDataConfirmacao(LocalDateTime.now());
        paymentRepository.save(payment);

        if (payment.getNecessidade() != null) {
            var need = payment.getNecessidade();
            need.setProgresso(Math.min(100, (need.getProgresso() != null ? need.getProgresso() : 0) + 5));
        }

        notificationService.notificar(
            payment.getInstituicao().getUser().getId(),
            "nova_doacao",
            principal.getUser().getNome() + " contribuiu com R$ " + payment.getValor()
                + " via " + payment.getMetodo() + ".",
            "/dashboard/instituicao"
        );

        return "redirect:/pagamento/" + uuid + "/sucesso";
    }

    @GetMapping("/{uuid}/sucesso")
    public String sucesso(@PathVariable String uuid, Model model) {
        Payment payment = paymentRepository.findByUuid(uuid).orElse(null);
        if (payment == null) return "redirect:/dashboard/doador";
        model.addAttribute("payment", payment);
        return "pagamento-sucesso";
    }

    @GetMapping("/{uuid}/comprovante")
    public String comprovantePagamento(@PathVariable String uuid,
                                        @AuthenticationPrincipal CustomUserDetails principal,
                                        Model model) {
        Payment payment = paymentRepository.findByUuid(uuid).orElse(null);
        if (payment == null || !payment.getDoador().getId().equals(principal.getUser().getId())) {
            return "redirect:/dashboard/doador";
        }
        model.addAttribute("tipo", "financeiro");
        model.addAttribute("valor", payment.getValor());
        model.addAttribute("metodo", payment.getMetodo());
        model.addAttribute("instituicao", payment.getInstituicao().getRazaoSocial());
        model.addAttribute("data", java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(payment.getDataCriacao()));
        model.addAttribute("status", payment.getStatus());
        return "comprovante";
    }
}
