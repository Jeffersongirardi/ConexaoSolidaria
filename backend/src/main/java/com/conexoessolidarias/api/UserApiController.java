package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.ChangePasswordRequest;
import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.api.dto.UpdateProfileRequest;
import com.conexoessolidarias.api.dto.UserDTO;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.UserRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
public class UserApiController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    public UserApiController(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             StorageService storageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserDTO> me(
            @AuthenticationPrincipal CustomUserDetails principal) {
        User user = userRepository.findById(principal.getUser().getId()).orElseThrow();
        return ResponseEntity.ok(UserDTO.from(user));
    }

    @PutMapping("/me")
    @Transactional
    public ResponseEntity<UserDTO> atualizar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateProfileRequest req) {
        User user = principal.getUser();
        user.setNome(req.nome());
        user.setTelefone(req.telefone());
        user.setWhatsapp(req.whatsapp());
        user.setCep(req.cep());
        user.setCidade(req.cidade());
        user.setEstado(req.estado());
        return ResponseEntity.ok(UserDTO.from(userRepository.save(user)));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<UserDTO> avatar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam("avatar") MultipartFile avatar) {
        User user = principal.getUser();
        String url = storageService.save(avatar, "avatars");
        if (url == null) {
            throw new IllegalArgumentException("Imagem inválida (png/jpg/gif/webp até 5MB)");
        }
        user.setAvatarUrl(url);
        return ResponseEntity.ok(UserDTO.from(userRepository.save(user)));
    }

    @PostMapping("/me/password")
    @Transactional
    public ResponseEntity<MessageResponse> alterarSenha(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ChangePasswordRequest req) {
        User user = principal.getUser();
        if (!passwordEncoder.matches(req.senhaAtual(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        if (!req.novaSenha().equals(req.confirmacao())) {
            throw new IllegalArgumentException("Nova senha e confirmação não conferem");
        }
        user.setPasswordHash(passwordEncoder.encode(req.novaSenha()));
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Senha alterada com sucesso!"));
    }
}
