package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_lembretes", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "referencia_id", "user_id"}))
public class EmailLembrete {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 40)
    private String tipo;
    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm = LocalDateTime.now();

    public EmailLembrete() {}
    public EmailLembrete(String tipo, Long referenciaId, Long userId) {
        this.tipo = tipo; this.referenciaId = referenciaId; this.userId = userId;
    }
    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public Long getReferenciaId() { return referenciaId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getEnviadoEm() { return enviadoEm; }
}
