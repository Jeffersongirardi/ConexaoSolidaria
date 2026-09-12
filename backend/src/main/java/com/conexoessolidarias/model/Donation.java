package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doador_id", nullable = false)
    private User doador;

    @ManyToOne
    @JoinColumn(name = "necessidade_id", nullable = false)
    private Need need;

    @Column(nullable = false)
    private String tipo = "fisico";

    @Column(nullable = false)
    private String item;

    @Column(nullable = false)
    private String quantidade;

    private String categoria = "outro";

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(nullable = false)
    private String status = "pendente";

    private LocalDateTime dataIntencao = LocalDateTime.now();

    private LocalDateTime dataRecebimento;

    @OneToMany(mappedBy = "donation")
    @OrderBy("dataCriacao DESC")
    private List<DonationUpdate> updates = new ArrayList<>();

    public Donation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getDoador() { return doador; }
    public void setDoador(User doador) { this.doador = doador; }
    public Need getNeed() { return need; }
    public void setNeed(Need need) { this.need = need; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
    public String getQuantidade() { return quantidade; }
    public void setQuantidade(String quantidade) { this.quantidade = quantidade; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDataIntencao() { return dataIntencao; }
    public void setDataIntencao(LocalDateTime dataIntencao) { this.dataIntencao = dataIntencao; }
    public LocalDateTime getDataRecebimento() { return dataRecebimento; }
    public void setDataRecebimento(LocalDateTime dataRecebimento) { this.dataRecebimento = dataRecebimento; }
    public List<DonationUpdate> getUpdates() { return updates; }
    public void setUpdates(List<DonationUpdate> updates) { this.updates = updates; }
}
