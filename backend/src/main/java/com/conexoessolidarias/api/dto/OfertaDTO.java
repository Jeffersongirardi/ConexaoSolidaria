package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.Oferta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OfertaDTO(
        Long id,
        String titulo,
        String descricao,
        String categoria,
        String estadoItem,
        String cidade,
        Boolean precisaColeta,
        String enderecoColeta,
        LocalDate disponivelAte,
        String status,
        String doadorNome,
        Long doadorId,
        String doadorWhatsapp,
        Long instituicaoId,
        String instituicaoNome,
        String instituicaoWhatsapp,
        LocalDate prazoColeta,
        LocalDateTime dataCriacao,
        Boolean aprovado,
        String motivoRecusa,
        List<ImagemDTO> imagens) {

    public record ImagemDTO(Long id, String url) {}

    public static OfertaDTO from(Oferta o) {
        return from(o, false);
    }

    /**
     * Contato e endereço do doador só são expostos após a reserva
     * (instituição reservante, doador dono ou admin). O padrão é ocultar.
     */
    public static OfertaDTO from(Oferta o, boolean exporContato) {
        List<ImagemDTO> imgs = o.getImages() != null
                ? o.getImages().stream()
                    .map(i -> new ImagemDTO(i.getId(), i.getFilename())).toList()
                : List.of();
        var inst = o.getInstituicao();
        return new OfertaDTO(o.getId(), o.getTitulo(), o.getDescricao(), o.getCategoria(),
                o.getEstadoItem(), o.getCidade(), o.getPrecisaColeta(),
                exporContato ? o.getEnderecoColeta() : null,
                o.getDisponivelAte(), o.getStatus(),
                o.getDoador() != null ? o.getDoador().getNome() : null,
                o.getDoador() != null ? o.getDoador().getId() : null,
                exporContato && o.getDoador() != null ? o.getDoador().getWhatsapp() : null,
                inst != null ? inst.getId() : null,
                inst != null ? inst.getRazaoSocial() : null,
                inst != null ? inst.getWhatsapp() : null,
                o.getPrazoColeta(), o.getDataCriacao(),
                o.getAprovado(), o.getMotivoRecusa(), imgs);
    }
}
