package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.PasswordResetToken;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.PasswordResetTokenRepository;
import com.conexoessolidarias.repository.UserRepository;
import com.conexoessolidarias.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetController(UserRepository userRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    PasswordEncoder passwordEncoder,
                                    EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/recuperar-senha")
    public String formRecuperar() {
        return "recuperar-senha";
    }

    @PostMapping("/recuperar-senha")
    public String solicitarRecuperacao(@RequestParam String email,
                                        @RequestParam(defaultValue = "doador") String tipo,
                                        RedirectAttributes redirect) {
        User user = userRepository.findByEmailAndTipo(email, tipo).orElse(null);
        if (user != null) {
            tokenRepository.deleteByUserId(user.getId());
            String token = UUID.randomUUID().toString();
            tokenRepository.save(new PasswordResetToken(token, user));
            String link = "http://localhost:8080/auth/redefinir-senha?token=" + token;
            emailService.enviarLinkRedefinicaoSenha(email, user.getNome(), link);
        }
        redirect.addFlashAttribute("success", "Se o e-mail estiver cadastrado, enviaremos um link de redefinição.");
        return "redirect:/auth/login-" + tipo;
    }

    @GetMapping("/redefinir-senha")
    public String formRedefinir(@RequestParam String token, Model model, RedirectAttributes redirect) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        if (resetToken == null || resetToken.isUsed() || resetToken.isExpired()) {
            redirect.addFlashAttribute("error", "Token inválido ou expirado.");
            return "redirect:/auth/login-doador";
        }
        model.addAttribute("token", token);
        return "redefinir-senha";
    }

    @PostMapping("/redefinir-senha")
    public String redefinirSenha(@RequestParam String token,
                                  @RequestParam String senha,
                                  @RequestParam String confirmacao,
                                  RedirectAttributes redirect) {
        if (!senha.equals(confirmacao)) {
            redirect.addFlashAttribute("error", "As senhas não conferem.");
            return "redirect:/auth/redefinir-senha?token=" + token;
        }
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        if (resetToken == null || resetToken.isUsed() || resetToken.isExpired()) {
            redirect.addFlashAttribute("error", "Token inválido ou expirado.");
            return "redirect:/auth/login-doador";
        }
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(senha));
        userRepository.save(user);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        redirect.addFlashAttribute("success", "Senha redefinida com sucesso! Faça login.");
        return "redirect:/auth/login-doador";
    }
}
