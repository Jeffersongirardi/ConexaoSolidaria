package com.conexoessolidarias.service;

import com.conexoessolidarias.model.PasswordResetToken;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Tokens de redefinição são guardados como hash (nunca em texto plano),
 * expiram em 30 min, são de uso único e têm limite de 3 pedidos por hora.
 */
@Service
public class PasswordResetService {

    public static final int MAX_POR_HORA = 3;
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository tokenRepository;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /** Retorna o token cru (para o e-mail) ou vazio quando o limite por hora foi atingido. */
    @Transactional
    public Optional<String> solicitar(User user) {
        long recentes = tokenRepository.countByUserIdAndCreatedAtAfter(
                user.getId(), LocalDateTime.now().minusHours(1));
        if (recentes >= MAX_POR_HORA) {
            log.warn("Limite de redefinições por hora atingido para o usuário {}", user.getId());
            return Optional.empty();
        }
        tokenRepository.marcarAnterioresComoUsados(user.getId());
        String raw = gerarToken();
        PasswordResetToken token = new PasswordResetToken(hash(raw), user);
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);
        return Optional.of(raw);
    }

    @Transactional
    public PasswordResetToken consumir(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }
        PasswordResetToken token = tokenRepository.findByToken(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));
        if (token.isUsed() || token.isExpired()) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }
        return token;
    }

    @Transactional
    public void marcarUsado(PasswordResetToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }

    public static String hash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private static String gerarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
