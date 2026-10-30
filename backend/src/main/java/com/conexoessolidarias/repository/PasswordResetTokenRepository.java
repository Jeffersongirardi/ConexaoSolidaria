package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime desde);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user.id = :userId AND t.used = false")
    void marcarAnterioresComoUsados(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
