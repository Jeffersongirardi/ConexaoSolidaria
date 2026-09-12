package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "needs")
public class Need {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstitutionProfile institution;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    private String categoria = "outro";

    @Column(nullable = false)
    private String quantidadeAlvo;

    private String urgencia = "media";

    private Boolean aceitaFinanceiro = false;

    private Integer progresso = 0;

    @Column(nullable = false)
    private Boolean ativo = true;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDateTime dataEncerramento;

    @OneToMany(mappedBy = "need")
    private List<Donation> donations = new ArrayList<>();

    @OneToMany(mappedBy = "need", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<NeedImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "necessidade")
    private List<Payment> pagamentos = new ArrayList<>();

    public Need() {}

    public int progressoPercentual() {
        return Math.min(100, progresso != null ? progresso : 0);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public InstitutionProfile getInstitution() { return institution; }
    public void setInstitution(InstitutionProfile institution) { this.institution = institution; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getQuantidadeAlvo() { return quantidadeAlvo; }
    public void setQuantidadeAlvo(String quantidadeAlvo) { this.quantidadeAlvo = quantidadeAlvo; }
    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }
    public Boolean getAceitaFinanceiro() { return aceitaFinanceiro; }
    public void setAceitaFinanceiro(Boolean aceitaFinanceiro) { this.aceitaFinanceiro = aceitaFinanceiro; }
    public Integer getProgresso() { return progresso; }
    public void setProgresso(Integer progresso) { this.progresso = progresso; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataEncerramento() { return dataEncerramento; }
    public void setDataEncerramento(LocalDateTime dataEncerramento) { this.dataEncerramento = dataEncerramento; }
    public List<Donation> getDonations() { return donations; }
    public void setDonations(List<Donation> donations) { this.donations = donations; }
    public List<NeedImage> getImages() { return images; }
    public void setImages(List<NeedImage> images) { this.images = images; }
    public List<Payment> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<Payment> pagamentos) { this.pagamentos = pagamentos; }
}
