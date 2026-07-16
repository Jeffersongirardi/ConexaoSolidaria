package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 300)
    private String mensagem;

    @Column(length = 200)
    private String link;

    @Column(nullable = false)
    private Boolean lida = false;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    public Notification() {}

    public Notification(User user, String tipo, String mensagem, String link) {
        this.user = user;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.link = link;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public Boolean getLida() { return lida; }
    public void setLida(Boolean lida) { this.lida = lida; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
