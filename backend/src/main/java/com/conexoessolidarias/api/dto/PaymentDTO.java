package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Function;

public record PaymentDTO(
        String uuid,
        BigDecimal valor,
        String metodo,
        String status,
        String comprovanteUrl,
        String transacaoId,
        LocalDateTime dataCriacao,
        LocalDateTime dataConfirmacao,
        String instituicaoNome,
        Long campaignId,
        String campaignTitulo,
        String pixKey,
        String copiaECola,
        String qrcode) {

    public static PaymentDTO from(Payment p) {
        return from(p, null);
    }

    public static PaymentDTO from(Payment p, String qrcode) {
        return from(p, qrcode, Function.identity());
    }

    public static PaymentDTO from(Payment p, String qrcode, Function<String, String> resolver) {
        return new PaymentDTO(p.getUuid(), p.getValor(), p.getMetodo(), p.getStatus(),
                resolver.apply(p.getComprovanteUrl()), p.getTransacaoId(), p.getDataCriacao(),
                p.getDataConfirmacao(),
                p.getInstituicao() != null ? p.getInstituicao().getRazaoSocial() : null,
                p.getCampaign() != null ? p.getCampaign().getId() : null,
                p.getCampaign() != null ? p.getCampaign().getTitulo() : null,
                p.getInstituicao() != null ? p.getInstituicao().getPixKey() : null,
                p.getCopiaECola(),
                qrcode);
    }
}
