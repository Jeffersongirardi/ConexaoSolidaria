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
        LocalDate prazoColeta,
        LocalDateTime dataCriacao,
        Boolean aprovado,
        String motivoRecusa,
        List<ImagemDTO> imagens) {

    public record ImagemDTO(Long id, String url) {}

    public static OfertaDTO from(Oferta o) {
        List<ImagemDTO> imgs = o.getImages() != null
                ? o.getImages().stream()
                    .map(i -> new ImagemDTO(i.getId(), i.getFilename())).toList()
                : List.of();
        var inst = o.getInstituicao();
        return new OfertaDTO(o.getId(), o.getTitulo(), o.getDescricao(), o.getCategoria(),
                o.getEstadoItem(), o.getCidade(), o.getPrecisaColeta(), o.getEnderecoColeta(),
                o.getDisponivelAte(), o.getStatus(),
                o.getDoador() != null ? o.getDoador().getNome() : null,
                o.getDoador() != null ? o.getDoador().getId() : null,
                o.getDoador() != null ? o.getDoador().getWhatsapp() : null,
                inst != null ? inst.getId() : null,
                inst != null ? inst.getRazaoSocial() : null,
                o.getPrazoColeta(), o.getDataCriacao(),
                o.getAprovado(), o.getMotivoRecusa(), imgs);
    }
}
