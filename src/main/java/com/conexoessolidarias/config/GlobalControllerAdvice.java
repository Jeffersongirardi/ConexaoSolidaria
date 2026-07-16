package com.conexoessolidarias.config;

import com.conexoessolidarias.repository.NeedRepository;
import com.conexoessolidarias.repository.NotificationRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final NeedRepository needRepository;
    private final NotificationRepository notificationRepository;

    public GlobalControllerAdvice(NeedRepository needRepository,
                                  NotificationRepository notificationRepository) {
        this.needRepository = needRepository;
        this.notificationRepository = notificationRepository;
    }

    @ModelAttribute("needCount")
    public long needCount() {
        return needRepository.count();
    }

    @ModelAttribute("notificacoesNaoLidas")
    public long notificacoesNaoLidas(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails user) {
            return notificationRepository.countByUserIdAndLidaFalse(user.getUser().getId());
        }
        return 0;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
