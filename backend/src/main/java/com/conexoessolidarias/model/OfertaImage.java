package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "oferta_images")
public class OfertaImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "oferta_id", nullable = false)
    private Oferta oferta;

    @Column(nullable = false, length = 200)
    private String filename;

    @Column(length = 200)
    private String legenda;

    private Integer ordem = 0;

    private LocalDateTime dataUpload = LocalDateTime.now();

    public OfertaImage() {}

    public OfertaImage(Oferta oferta, String filename) {
        this.oferta = oferta;
        this.filename = filename;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Oferta getOferta() { return oferta; }
    public void setOferta(Oferta oferta) { this.oferta = oferta; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getLegenda() { return legenda; }
    public void setLegenda(String legenda) { this.legenda = legenda; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
}
