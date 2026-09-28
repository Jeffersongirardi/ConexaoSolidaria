package com.conexoessolidarias.service;

import com.conexoessolidarias.repository.DonationRepository;
import com.conexoessolidarias.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Métricas públicas de campanhas — só o quantificável e confirmado:
 * - doadores distintos (intenções não canceladas);
 * - R$ recebido (pagamentos confirmados pela instituição);
 * - itens recebidos por categoria (doações confirmadas pela instituição).
 * Detalhes (o que foi doado, valores por doador) seguem restritos aos painéis.
 */
@Service
public class CampaignStatsService {

    public record CampaignMetric(int numDoadores, BigDecimal valorRecebido,
                                 int numDoacoesItens, Map<String, Integer> itensPorCategoria) {
        public static CampaignMetric vazio() {
            return new CampaignMetric(0, BigDecimal.ZERO, 0, Collections.emptyMap());
        }
    }

    private final DonationRepository donationRepository;
    private final PaymentRepository paymentRepository;

    public CampaignStatsService(DonationRepository donationRepository,
                                PaymentRepository paymentRepository) {
        this.donationRepository = donationRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public Map<Long, CampaignMetric> metricas(Collection<Long> campaignIds) {
        if (campaignIds == null || campaignIds.isEmpty()) return Collections.emptyMap();
        List<Long> ids = List.copyOf(campaignIds);

        Map<Long, Set<Long>> doadores = new HashMap<>();
        for (Object[] row : donationRepository.findDoadorIdsByCampaignIds(ids)) {
            doadores.computeIfAbsent((Long) row[0], k -> new HashSet<>()).add((Long) row[1]);
        }
        for (Object[] row : paymentRepository.findDoadorIdsByCampaignIds(ids)) {
            doadores.computeIfAbsent((Long) row[0], k -> new HashSet<>()).add((Long) row[1]);
        }

        Map<Long, BigDecimal> valores = new HashMap<>();
        for (Object[] row : paymentRepository.sumValorRecebidoPorCampanha(ids)) {
            valores.put((Long) row[0], (BigDecimal) row[1]);
        }

        Map<Long, Map<String, Integer>> itens = new HashMap<>();
        for (Object[] row : countItens(ids)) {
            String categoria = row[1] != null ? (String) row[1] : "outro";
            itens.computeIfAbsent((Long) row[0], k -> new TreeMap<>())
                    .merge(categoria, ((Number) row[2]).intValue(), Integer::sum);
        }

        Map<Long, CampaignMetric> resultado = new HashMap<>();
        for (Long id : ids) {
            Set<Long> set = doadores.get(id);
            Map<String, Integer> porCategoria = itens.getOrDefault(id, Collections.emptyMap());
            int numItens = porCategoria.values().stream().mapToInt(Integer::intValue).sum();
            resultado.put(id, new CampaignMetric(
                    set != null ? set.size() : 0,
                    valores.getOrDefault(id, BigDecimal.ZERO),
                    numItens, porCategoria));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> contarDoadores(Collection<Long> campaignIds) {
        Map<Long, CampaignMetric> metricas = metricas(campaignIds);
        Map<Long, Integer> totais = new HashMap<>();
        metricas.forEach((id, m) -> totais.put(id, m.numDoadores()));
        return totais;
    }

    private List<Object[]> countItens(List<Long> ids) {
        return donationRepository.countItensRecebidosPorCategoria(ids);
    }
}
