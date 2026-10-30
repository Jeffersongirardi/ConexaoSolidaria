package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.EmailService;
import com.conexoessolidarias.service.EmailVerificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class EmailVerificationApiController {

    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public EmailVerificationApiController(EmailVerificationService emailVerificationService,
                                          EmailService emailService) {
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
    }

    public record TokenRequest(@NotBlank String token) {
    }

    @PostMapping("/verify-email")
    @Transactional
    public ResponseEntity<MessageResponse> verificar(@Valid @RequestBody TokenRequest req) {
        emailVerificationService.verificar(req.token());
        return ResponseEntity.ok(new MessageResponse("E-mail confirmado com sucesso!"));
    }

    @PostMapping("/resend-verification")
    @Transactional
    public ResponseEntity<MessageResponse> reenviar(
            @AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getUser();
        if (Boolean.TRUE.equals(user.getEmailVerificado())) {
            return ResponseEntity.ok(new MessageResponse("Seu e-mail já está confirmado."));
        }
        emailVerificationService.gerar(user).ifPresent(token ->
                emailService.enviarVerificacaoEmail(user.getEmail(), user.getNome(),
                        frontendUrl + "/verificar-email?token=" + token));
        return ResponseEntity.ok(new MessageResponse(
                "Se necessário, enviaremos um novo link de confirmação para o seu e-mail."));
    }
}
