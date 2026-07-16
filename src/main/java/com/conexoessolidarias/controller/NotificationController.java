package com.conexoessolidarias.controller;

import com.conexoessolidarias.repository.NotificationRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/notificacoes")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public NotificationController(NotificationRepository notificationRepository,
                                  NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        if (principal == null) return "redirect:/";
        model.addAttribute("notificacoes",
            notificationRepository.findByUserIdOrderByDataCriacaoDesc(principal.getUser().getId()));
        return "notificacoes";
    }

    @GetMapping("/ler/{id}")
    public String ler(@PathVariable Long id,
                      @AuthenticationPrincipal CustomUserDetails principal) {
        var notif = notificationRepository.findById(id).orElse(null);
        if (notif == null || !notif.getUser().getId().equals(principal.getUser().getId())) {
            return "redirect:/notificacoes";
        }
        notif.setLida(true);
        notificationRepository.save(notif);
        if (notif.getLink() != null) return "redirect:" + notif.getLink();
        return "redirect:/notificacoes";
    }

    @GetMapping("/ler-todas")
    public String lerTodas(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal != null) {
            notificationService.marcarTodasComoLidas(principal.getUser().getId());
        }
        return "redirect:/notificacoes";
    }

    @GetMapping("/nao-lidas")
    @ResponseBody
    public java.util.Map<String, Long> naoLidasCount(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) return java.util.Map.of("count", 0L);
        return java.util.Map.of("count",
            notificationRepository.countByUserIdAndLidaFalse(principal.getUser().getId()));
    }
}
