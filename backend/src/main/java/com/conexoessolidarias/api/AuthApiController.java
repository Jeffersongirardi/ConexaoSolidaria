package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.*;
import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.PasswordResetToken;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.PasswordResetTokenRepository;
import com.conexoessolidarias.repository.UserRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import com.conexoessolidarias.security.JwtService;
import com.conexoessolidarias.service.EmailService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final UserRepository userRepository;
    private final InstitutionProfileRepository institutionProfileRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public AuthApiController(UserRepository userRepository,
                             InstitutionProfileRepository institutionProfileRepository,
                             PasswordResetTokenRepository tokenRepository,
                             PasswordEncoder passwordEncoder,
                             AuthenticationManager authenticationManager,
                             JwtService jwtService,
                             EmailService emailService) {
        this.userRepository = userRepository;
        this.institutionProfileRepository = institutionProfileRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @PostMapping("/register/doador")
    @Transactional
    public ResponseEntity<UserDTO> registerDoador(@Valid @RequestBody RegisterDoadorRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalStateException("E-mail já cadastrado");
        }
        User user = new User(req.nome(), req.email(),
                passwordEncoder.encode(req.senha()), "doador");
        user.setTelefone(req.telefone());
        user.setCpf(req.cpf());
        user.setWhatsapp(req.whatsapp());
        user.setCep(req.cep());
        user.setCidade(req.cidade());
        user.setEstado(req.estado());
        if (req.dataNascimento() != null && !req.dataNascimento().isBlank()) {
            user.setDataNascimento(LocalDate.parse(req.dataNascimento()));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDTO.from(userRepository.save(user)));
    }

    @PostMapping("/register/instituicao")
    @Transactional
    public ResponseEntity<UserDTO> registerInstituicao(
            @Valid @RequestBody RegisterInstituicaoRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalStateException("E-mail já cadastrado");
        }
        User user = new User(req.nome(), req.email(),
                passwordEncoder.encode(req.senha()), "instituicao");
        user.setTelefone(req.telefone());
        user.setWhatsapp(req.whatsapp());
        user = userRepository.save(user);

        InstitutionProfile profile = new InstitutionProfile();
        profile.setUser(user);
        profile.setCnpj(req.cnpj());
        profile.setRazaoSocial(req.razaoSocial());
        profile.setNomeFantasia(req.nomeFantasia());
        profile.setEndereco(req.endereco());
        profile.setDescricao(req.descricao());
        profile.setCategoriaAtuacao(req.categoriaAtuacao());
        profile.setWhatsapp(req.whatsapp());
        profile.setPixKey(req.pixKey());
        profile.setPixTitular(req.pixTitular());
        institutionProfileRepository.save(profile);

        user.setInstitutionProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDTO.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.senha()));
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        User user = details.getUser();
        return ResponseEntity.ok(buildAuthResponse(user));
    }

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        Claims claims;
        try {
            claims = jwtService.parse(req.refreshToken());
        } catch (Exception e) {
            throw new IllegalArgumentException("Refresh token inválido");
        }
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Refresh token inválido");
        }
        User user = userRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (!Boolean.TRUE.equals(user.getAtivo())) {
            throw new IllegalArgumentException("Usuário desativado");
        }
        return ResponseEntity.ok(buildAuthResponse(user));
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {
        userRepository.findByEmail(req.email()).ifPresent(user -> {
            tokenRepository.deleteByUserId(user.getId());
            String token = UUID.randomUUID().toString();
            tokenRepository.save(new PasswordResetToken(token, user));
            String link = frontendUrl + "/redefinir-senha?token=" + token;
            emailService.enviarLinkRedefinicaoSenha(user.getEmail(), user.getNome(), link);
        });
        return ResponseEntity.ok(new MessageResponse(
                "Se o e-mail estiver cadastrado, enviaremos um link de redefinição."));
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {
        if (!req.senha().equals(req.confirmacao())) {
            throw new IllegalArgumentException("As senhas não conferem");
        }
        PasswordResetToken resetToken = tokenRepository.findByToken(req.token())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));
        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(req.senha()));
        userRepository.save(user);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        return ResponseEntity.ok(new MessageResponse("Senha redefinida com sucesso!"));
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user), "Bearer",
                jwtService.getAccessTtlSeconds(), UserDTO.from(user));
    }
}
