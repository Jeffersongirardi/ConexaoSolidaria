package com.conexoessolidarias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "need_images")
public class NeedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "need_id", nullable = false)
    private Need need;

    @Column(nullable = false, length = 200)
    private String filename;

    @Column(length = 200)
    private String legenda;

    private Integer ordem = 0;

    private LocalDateTime dataUpload = LocalDateTime.now();

    public NeedImage() {}

    public NeedImage(Need need, String filename) {
        this.need = need;
        this.filename = filename;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Need getNeed() { return need; }
    public void setNeed(Need need) { this.need = need; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getLegenda() { return legenda; }
    public void setLegenda(String legenda) { this.legenda = legenda; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
}
