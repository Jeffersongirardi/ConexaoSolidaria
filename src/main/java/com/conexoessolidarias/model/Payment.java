package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 36)
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "doador_id", nullable = false)
    private User doador;

    @ManyToOne
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstitutionProfile instituicao;

    @ManyToOne
    @JoinColumn(name = "necessidade_id")
    private Need necessidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 20)
    private String metodo;

    @Column(nullable = false)
    private String status = "pendente";

    @Column(length = 500)
    private String comprovanteUrl;

    @Column(length = 100)
    private String transacaoId;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDateTime dataConfirmacao;

    public Payment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public User getDoador() { return doador; }
    public void setDoador(User doador) { this.doador = doador; }
    public InstitutionProfile getInstituicao() { return instituicao; }
    public void setInstituicao(InstitutionProfile instituicao) { this.instituicao = instituicao; }
    public Need getNecessidade() { return necessidade; }
    public void setNecessidade(Need necessidade) { this.necessidade = necessidade; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getComprovanteUrl() { return comprovanteUrl; }
    public void setComprovanteUrl(String comprovanteUrl) { this.comprovanteUrl = comprovanteUrl; }
    public String getTransacaoId() { return transacaoId; }
    public void setTransacaoId(String transacaoId) { this.transacaoId = transacaoId; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(LocalDateTime dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }
}
