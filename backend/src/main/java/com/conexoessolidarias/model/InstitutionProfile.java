package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "institution_profiles")
public class InstitutionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(unique = true, nullable = false, length = 18)
    private String cnpj;

    @Column(nullable = false)
    private String razaoSocial;

    private String nomeFantasia;

    @Column(length = 300)
    private String endereco;

    private String website;

    @Column(length = 500)
    private String fotoUrl;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String categoriaAtuacao;

    @Column(length = 20)
    private String whatsapp;

    @Column(length = 100)
    private String pixKey;

    @Column(length = 100)
    private String pixTitular;

    @Column(nullable = false)
    private Boolean aprovado = false;

    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String motivoRecusa;

    @OneToMany(mappedBy = "institution")
    private List<Need> needs = new ArrayList<>();

    @OneToMany(mappedBy = "instituicao")
    private List<Payment> pagamentos = new ArrayList<>();

    public InstitutionProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCategoriaAtuacao() { return categoriaAtuacao; }
    public void setCategoriaAtuacao(String categoriaAtuacao) { this.categoriaAtuacao = categoriaAtuacao; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getPixKey() { return pixKey; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }
    public String getPixTitular() { return pixTitular; }
    public void setPixTitular(String pixTitular) { this.pixTitular = pixTitular; }
    public Boolean getAprovado() { return aprovado; }
    public void setAprovado(Boolean aprovado) { this.aprovado = aprovado; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public void setMotivoRecusa(String motivoRecusa) { this.motivoRecusa = motivoRecusa; }
    public List<Need> getNeeds() { return needs; }
    public void setNeeds(List<Need> needs) { this.needs = needs; }
    public List<Payment> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<Payment> pagamentos) { this.pagamentos = pagamentos; }
}
