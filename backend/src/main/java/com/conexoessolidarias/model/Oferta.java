package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ofertas")
public class Oferta {

    public static final String DISPONIVEL = "disponivel";
    public static final String RESERVADA = "reservada";
    public static final String ENTREGUE = "entregue";
    public static final String CANCELADA = "cancelada";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doador_id", nullable = false)
    private User doador;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    private String categoria = "outro";

    private String estadoItem = "usado";

    private String cidade;

    @Column(nullable = false)
    private Boolean precisaColeta = false;

    @Column(length = 300)
    private String enderecoColeta;

    @Column(nullable = false)
    private LocalDate disponivelAte;

    @Column(nullable = false)
    private String status = DISPONIVEL;

    @ManyToOne
    @JoinColumn(name = "instituicao_id")
    private InstitutionProfile instituicao;

    private LocalDateTime reservadaEm;

    private LocalDate prazoColeta;

    private LocalDateTime dataEntrega;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean aprovado = false;

    @Column(columnDefinition = "TEXT")
    private String motivoRecusa;

    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<OfertaImage> images = new ArrayList<>();

    public Oferta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getDoador() { return doador; }
    public void setDoador(User doador) { this.doador = doador; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getEstadoItem() { return estadoItem; }
    public void setEstadoItem(String estadoItem) { this.estadoItem = estadoItem; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public Boolean getPrecisaColeta() { return precisaColeta; }
    public void setPrecisaColeta(Boolean precisaColeta) { this.precisaColeta = precisaColeta; }
    public String getEnderecoColeta() { return enderecoColeta; }
    public void setEnderecoColeta(String enderecoColeta) { this.enderecoColeta = enderecoColeta; }
    public LocalDate getDisponivelAte() { return disponivelAte; }
    public void setDisponivelAte(LocalDate disponivelAte) { this.disponivelAte = disponivelAte; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public InstitutionProfile getInstituicao() { return instituicao; }
    public void setInstituicao(InstitutionProfile instituicao) { this.instituicao = instituicao; }
    public LocalDateTime getReservadaEm() { return reservadaEm; }
    public void setReservadaEm(LocalDateTime reservadaEm) { this.reservadaEm = reservadaEm; }
    public LocalDate getPrazoColeta() { return prazoColeta; }
    public void setPrazoColeta(LocalDate prazoColeta) { this.prazoColeta = prazoColeta; }
    public LocalDateTime getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(LocalDateTime dataEntrega) { this.dataEntrega = dataEntrega; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Boolean getAprovado() { return aprovado; }
    public void setAprovado(Boolean aprovado) { this.aprovado = aprovado; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public void setMotivoRecusa(String motivoRecusa) { this.motivoRecusa = motivoRecusa; }
    public List<OfertaImage> getImages() { return images; }
    public void setImages(List<OfertaImage> images) { this.images = images; }
}
