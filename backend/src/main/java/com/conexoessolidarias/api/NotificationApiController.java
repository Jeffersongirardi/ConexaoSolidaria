package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.api.dto.NotificationDTO;
import com.conexoessolidarias.model.Notification;
import com.conexoessolidarias.repository.NotificationRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationApiController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public NotificationApiController(NotificationRepository notificationRepository,
                                     NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> listar(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(notificationRepository
                .findByUserIdOrderByDataCriacaoDesc(principal.getUser().getId())
                .stream().map(NotificationDTO::from).toList());
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<Map<String, Long>> naoLidas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(Map.of("count", notificationService
                .contarNaoLidas(principal.getUser().getId())));
    }

    @PatchMapping("/{id}/ler")
    @Transactional
    public ResponseEntity<NotificationDTO> ler(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notificação não encontrada"));
        if (!notif.getUser().getId().equals(principal.getUser().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão");
        }
        notif.setLida(true);
        return ResponseEntity.ok(NotificationDTO.from(notificationRepository.save(notif)));
    }

    @PatchMapping("/ler-todas")
    @Transactional
    public ResponseEntity<MessageResponse> lerTodas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        notificationService.marcarTodasComoLidas(principal.getUser().getId());
        return ResponseEntity.ok(new MessageResponse("Todas marcadas como lidas"));
    }
}
