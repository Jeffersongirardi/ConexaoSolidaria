package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.Donation;
import com.conexoessolidarias.model.DonationUpdate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public record DonationDTO(
        Long id,
        String item,
        String quantidade,
        String categoria,
        String observacao,
        String status,
        LocalDateTime dataIntencao,
        LocalDateTime dataRecebimento,
        String motivoRejeicao,
        String doadorNome,
        String doadorWhatsapp,
        Long campaignId,
        String campaignTitulo,
        String instituicaoNome,
        String instituicaoWhatsapp,
        String instituicaoEndereco,
        String instrucoesEntrega,
        Boolean precisaColeta,
        String enderecoColeta,
        List<UpdateDTO> updates) {

    public record UpdateDTO(Long id, String mensagem, String fotoUrl, LocalDateTime dataCriacao) {
        public static UpdateDTO from(DonationUpdate u) {
            return new UpdateDTO(u.getId(), u.getMensagem(), u.getFotoUrl(), u.getDataCriacao());
        }
    }

    public static DonationDTO from(Donation d) {
        return from(d, Function.identity());
    }

    public static DonationDTO from(Donation d, Function<String, String> resolver) {
        List<UpdateDTO> ups = d.getUpdates() != null
                ? d.getUpdates().stream().map(u -> new UpdateDTO(u.getId(), u.getMensagem(), resolver.apply(u.getFotoUrl()), u.getDataCriacao())).toList()
                : List.of();
        var camp = d.getCampaign();
        var inst = camp != null ? camp.getInstitution() : null;
        String endEntrega = camp != null && camp.getEnderecoEntrega() != null && !camp.getEnderecoEntrega().isBlank()
                ? camp.getEnderecoEntrega()
                : (inst != null ? inst.getEndereco() : null);
        return new DonationDTO(d.getId(), d.getItem(), d.getQuantidade(), d.getCategoria(),
                d.getObservacao(), d.getStatus(), d.getDataIntencao(), d.getDataRecebimento(),
                d.getMotivoRejeicao(),
                d.getDoador() != null ? d.getDoador().getNome() : null,
                d.getDoador() != null ? d.getDoador().getWhatsapp() : null,
                camp != null ? camp.getId() : null,
                camp != null ? camp.getTitulo() : null,
                inst != null ? inst.getRazaoSocial() : null,
                inst != null ? inst.getWhatsapp() : null,
                endEntrega,
                camp != null ? camp.getInstrucoesEntrega() : null,
                d.getPrecisaColeta(),
                d.getEnderecoColeta(),
                ups);
    }
}
