package com.conexoessolidarias.dto;

public class StatsDTO {
    private long doacoes;
    private long instituicoes;
    private long usuarios;
    private long necessidades;
    private long mensagensNaoLidas;
    private long instituicoesPendentes;

    public StatsDTO() {}

    public long getDoacoes() { return doacoes; }
    public void setDoacoes(long doacoes) { this.doacoes = doacoes; }
    public long getInstituicoes() { return instituicoes; }
    public void setInstituicoes(long instituicoes) { this.instituicoes = instituicoes; }
    public long getUsuarios() { return usuarios; }
    public void setUsuarios(long usuarios) { this.usuarios = usuarios; }
    public long getNecessidades() { return necessidades; }
    public void setNecessidades(long necessidades) { this.necessidades = necessidades; }
    public long getMensagensNaoLidas() { return mensagensNaoLidas; }
    public void setMensagensNaoLidas(long mensagensNaoLidas) { this.mensagensNaoLidas = mensagensNaoLidas; }
    public long getInstituicoesPendentes() { return instituicoesPendentes; }
    public void setInstituicoesPendentes(long instituicoesPendentes) { this.instituicoesPendentes = instituicoesPendentes; }
}
