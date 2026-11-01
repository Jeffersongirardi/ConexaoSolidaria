package com.conexoessolidarias.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_events")
public class AuthEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String evento;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    public AuthEvent() {
    }

    public AuthEvent(Long userId, String email, String evento, String ip, String userAgent) {
        this.userId = userId;
        this.email = email;
        this.evento = evento;
        this.ip = ip;
        this.userAgent = userAgent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
