package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByDataCriacaoDesc(Long userId);
    long countByUserIdAndLidaFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.lida = true WHERE n.user.id = :userId AND n.lida = false")
    void marcarTodasComoLidas(Long userId);
}
