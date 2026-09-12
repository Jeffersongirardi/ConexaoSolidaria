package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.UserRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.FileStorageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class ProfileController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository,
                             FileStorageService fileStorageService,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String editarForm() {
        return "perfil";
    }

    @PostMapping
    public String editar(@AuthenticationPrincipal CustomUserDetails principal,
                         @RequestParam String nome,
                         @RequestParam(required = false) String telefone,
                         @RequestParam(required = false) String whatsapp,
                         @RequestParam(required = false) String cep,
                         @RequestParam(required = false) String cidade,
                         @RequestParam(required = false) String estado,
                         @RequestParam("avatar") MultipartFile avatar,
                         RedirectAttributes redirect) {
        User user = principal.getUser();
        user.setNome(nome);
        user.setTelefone(telefone);
        user.setWhatsapp(whatsapp);
        user.setCep(cep);
        user.setCidade(cidade);
        user.setEstado(estado);

        if (avatar != null && !avatar.isEmpty()) {
            String url = fileStorageService.save(avatar, "avatars");
            if (url != null) user.setAvatarUrl(url);
        }

        if ("instituicao".equals(user.getTipo()) && user.getInstitutionProfile() != null) {
            var p = user.getInstitutionProfile();
            // institution-specific fields updated via separate form in the same page
            userRepository.save(user);
        }

        userRepository.save(user);
        redirect.addFlashAttribute("success", "Perfil atualizado!");
        return "redirect:/perfil";
    }

    @GetMapping("/alterar-senha")
    public String alterarSenhaForm() {
        return "alterar-senha";
    }

    @PostMapping("/alterar-senha")
    public String alterarSenha(@AuthenticationPrincipal CustomUserDetails principal,
                               @RequestParam String senhaAtual,
                               @RequestParam String novaSenha,
                               @RequestParam String confirmarSenha,
                               RedirectAttributes redirect) {
        User user = principal.getUser();
        if (!passwordEncoder.matches(senhaAtual, user.getPasswordHash())) {
            redirect.addFlashAttribute("error", "Senha atual incorreta.");
            return "redirect:/perfil/alterar-senha";
        }
        if (!novaSenha.equals(confirmarSenha)) {
            redirect.addFlashAttribute("error", "Nova senha e confirmação não conferem.");
            return "redirect:/perfil/alterar-senha";
        }
        if (novaSenha.length() < 6) {
            redirect.addFlashAttribute("error", "Senha deve ter no mínimo 6 caracteres.");
            return "redirect:/perfil/alterar-senha";
        }
        user.setPasswordHash(passwordEncoder.encode(novaSenha));
        userRepository.save(user);
        redirect.addFlashAttribute("success", "Senha alterada com sucesso!");
        return "redirect:/perfil";
    }
}
