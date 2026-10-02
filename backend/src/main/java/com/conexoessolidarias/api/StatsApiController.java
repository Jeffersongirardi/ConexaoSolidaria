package com.conexoessolidarias.api;

import com.conexoessolidarias.model.Oferta;
import com.conexoessolidarias.repository.CampaignRepository;
import com.conexoessolidarias.repository.DonationRepository;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.OfertaRepository;
import com.conexoessolidarias.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsApiController {

    private final DonationRepository donationRepository;
    private final InstitutionProfileRepository institutionProfileRepository;
    private final CampaignRepository campaignRepository;
    private final OfertaRepository ofertaRepository;
    private final PaymentRepository paymentRepository;

    public StatsApiController(DonationRepository donationRepository,
                              InstitutionProfileRepository institutionProfileRepository,
                              CampaignRepository campaignRepository,
                              OfertaRepository ofertaRepository,
                              PaymentRepository paymentRepository) {
        this.donationRepository = donationRepository;
        this.institutionProfileRepository = institutionProfileRepository;
        this.campaignRepository = campaignRepository;
        this.ofertaRepository = ofertaRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
                "doacoesRecebidas", donationRepository.countByStatus("recebido")
                        + paymentRepository.countByStatus("recebido")
                        + ofertaRepository.countByStatus(Oferta.ENTREGUE),
                "instituicoes", institutionProfileRepository.countByAprovado(true),
                "campanhasAtivas", campaignRepository.countByAtivoTrue()));
    }
}
