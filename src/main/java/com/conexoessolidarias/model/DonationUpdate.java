package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_updates")
public class DonationUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Column(length = 500)
    private String fotoUrl;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    public DonationUpdate() {}

    public DonationUpdate(Donation donation, String mensagem) {
        this.donation = donation;
        this.mensagem = mensagem;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Donation getDonation() { return donation; }
    public void setDonation(Donation donation) { this.donation = donation; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
