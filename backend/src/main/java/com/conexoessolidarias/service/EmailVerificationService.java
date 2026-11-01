package com.conexoessolidarias.service;

import com.conexoessolidarias.model.EmailVerificationToken;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.EmailVerificationTokenRepository;
import com.conexoessolidarias.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Verificação de e-mail: token hasheado, validade de 24h, uso único e limite de
 * 3 reenvios por hora. O bloqueio de ações sensíveis é controlado pela flag
 * {@code app.security.require-email-verification} (desligada por padrão).
 */
@Service
public class EmailVerificationService {

    public static final int MAX_POR_HORA = 3;
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Value("${app.security.require-email-verification:false}")
    private boolean requireVerification;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository,
                                    UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    public boolean isRequired() {
        return requireVerification;
    }

    public void exigirVerificado(User user) {
        if (requireVerification && !Boolean.TRUE.equals(user.getEmailVerificado())) {
            throw new EmailNaoVerificadoException(
                    "Confirme seu e-mail para continuar. Verifique sua caixa de entrada ou reenvie o link.");
        }
    }

    /** Retorna o token cru (para o e-mail) ou vazio quando o limite por hora foi atingido. */
    @Transactional
    public Optional<String> gerar(User user) {
        long recentes = tokenRepository.countByUserIdAndCreatedAtAfter(
                user.getId(), LocalDateTime.now().minusHours(1));
        if (recentes >= MAX_POR_HORA) {
            log.warn("Limite de verificações por hora atingido para o usuário {}", user.getId());
            return Optional.empty();
        }
        tokenRepository.marcarAnterioresComoUsados(user.getId());
        String raw = gerarToken();
        EmailVerificationToken token = new EmailVerificationToken(PasswordResetService.hash(raw), user);
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);
        return Optional.of(raw);
    }

    @Transactional
    public void verificar(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }
        EmailVerificationToken token = tokenRepository.findByToken(PasswordResetService.hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));
        if (token.isUsed() || token.isExpired()) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }
        User user = token.getUser();
        user.setEmailVerificado(true);
        user.setEmailVerificadoEm(LocalDateTime.now());
        userRepository.save(user);
        token.setUsed(true);
        tokenRepository.save(token);
    }

    private static String gerarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
