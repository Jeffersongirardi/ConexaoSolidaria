package com.conexoessolidarias.api;

import com.conexoessolidarias.repository.CampaignRepository;
import com.conexoessolidarias.repository.DonationRepository;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
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

    public StatsApiController(DonationRepository donationRepository,
                              InstitutionProfileRepository institutionProfileRepository,
                              CampaignRepository campaignRepository) {
        this.donationRepository = donationRepository;
        this.institutionProfileRepository = institutionProfileRepository;
        this.campaignRepository = campaignRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
                "doacoesRecebidas", donationRepository.countByStatus("recebido"),
                "instituicoes", institutionProfileRepository.countByAprovado(true),
                "campanhasAtivas", campaignRepository.countByAtivoTrue()));
    }
}
