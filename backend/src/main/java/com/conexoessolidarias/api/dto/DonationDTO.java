package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.Donation;
import com.conexoessolidarias.model.DonationUpdate;
import java.time.LocalDateTime;
import java.util.List;

public record DonationDTO(
        Long id,
        String item,
        String quantidade,
        String categoria,
        String observacao,
        String status,
        LocalDateTime dataIntencao,
        LocalDateTime dataRecebimento,
        String doadorNome,
        Long campaignId,
        String campaignTitulo,
        String instituicaoNome,
        List<UpdateDTO> updates) {

    public record UpdateDTO(Long id, String mensagem, String fotoUrl, LocalDateTime dataCriacao) {
        public static UpdateDTO from(DonationUpdate u) {
            return new UpdateDTO(u.getId(), u.getMensagem(), u.getFotoUrl(), u.getDataCriacao());
        }
    }

    public static DonationDTO from(Donation d) {
        List<UpdateDTO> ups = d.getUpdates() != null
                ? d.getUpdates().stream().map(UpdateDTO::from).toList()
                : List.of();
        return new DonationDTO(d.getId(), d.getItem(), d.getQuantidade(), d.getCategoria(),
                d.getObservacao(), d.getStatus(), d.getDataIntencao(), d.getDataRecebimento(),
                d.getDoador() != null ? d.getDoador().getNome() : null,
                d.getCampaign() != null ? d.getCampaign().getId() : null,
                d.getCampaign() != null ? d.getCampaign().getTitulo() : null,
                d.getCampaign() != null && d.getCampaign().getInstitution() != null
                        ? d.getCampaign().getInstitution().getRazaoSocial() : null,
                ups);
    }
}
