package com.conexoessolidarias.api;

import com.conexoessolidarias.model.Campaign;
import com.conexoessolidarias.model.Donation;
import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.Payment;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.CampaignRepository;
import com.conexoessolidarias.repository.DonationRepository;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.PaymentRepository;
import com.conexoessolidarias.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Instituição recusada pelo admin não pode editar/reativar/excluir campanha,
 * confirmar doações/pagamentos, publicar atualizações nem ler dados de doadores.
 * Fixture única (RateLimitFilter compartilha a janela de 20 req/min em /auth/*).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModeracaoInstituicaoApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InstitutionProfileRepository profileRepository;
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;

    private String tokenInstituicao;
    private long campaignId;
    private long donationId;
    private String paymentUuid;

    private String token(String email, String senha) throws Exception {
        MvcResult login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @BeforeAll
    void prepararCenario() throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        String emailInst = "moderacao-inst-" + sufixo + "@teste.org";
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));

        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Moderacao\",\"email\":\"" + emailInst + "\",\"senha\":\"senha123\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Moderacao\"}"))
                .andExpect(status().isCreated());

        InstitutionProfile profile = profileRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(emailInst)).findFirst().orElseThrow();
        profile.setAprovado(true);
        profileRepository.save(profile);

        tokenInstituicao = token(emailInst, "senha123");

        mvc.perform(put("/api/v1/institutions/minha")
                        .header("Authorization", "Bearer " + tokenInstituicao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pixKey\":\"" + emailInst + "\"}"))
                .andExpect(status().isOk());

        MvcResult campanha = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInstituicao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Campanha Moderacao","descricao":"Teste de moderacao",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media",
                                "aceitaFinanceiro":true}"""))
                .andExpect(status().isCreated()).andReturn();
        campaignId = objectMapper.readTree(campanha.getResponse().getContentAsString()).get("id").asLong();
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();

        User doador = new User("Doador Moderacao", "moderacao-doador-" + sufixo + "@teste.org", "hash", "doador");
        doador.setAtivo(true);
        doador = userRepository.save(doador);

        Donation donation = new Donation();
        donation.setDoador(doador);
        donation.setCampaign(campaign);
        donation.setItem("Arroz");
        donation.setQuantidade("1");
        donation = donationRepository.save(donation);
        donationId = donation.getId();

        Payment payment = new Payment();
        payment.setDoador(doador);
        payment.setInstituicao(profile);
        payment.setCampaign(campaign);
        payment.setValor(new BigDecimal("10.00"));
        payment.setMetodo("pix");
        paymentUuid = paymentRepository.save(payment).getUuid();
    }

    @Test
    void instituicaoRecusadaFicaBloqueadaParaOperar() throws Exception {
        // Antes da recusa a instituição opera normalmente.
        mvc.perform(patch("/api/v1/campaigns/" + campaignId + "/toggle")
                        .header("Authorization", "Bearer " + tokenInstituicao))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/v1/campaigns/" + campaignId + "/toggle")
                        .header("Authorization", "Bearer " + tokenInstituicao))
                .andExpect(status().isOk());

        String tokenAdmin = token("admin@conexoessolidarias.org", "admin123");
        long profileId = campaignRepository.findById(campaignId).orElseThrow()
                .getInstitution().getId();
        mvc.perform(patch("/api/v1/admin/institutions/" + profileId + "/recusar")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Teste de moderacao\"}"))
                .andExpect(status().isOk());

        // Escrita: editar/reativar/excluir campanha.
        mvc.perform(put("/api/v1/campaigns/" + campaignId)
                        .header("Authorization", "Bearer " + tokenInstituicao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Alterada","descricao":"Nao deveria",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media"}"""))
                .andExpect(status().is4xxClientError());
        mvc.perform(patch("/api/v1/campaigns/" + campaignId + "/toggle")
                        .header("Authorization", "Bearer " + tokenInstituicao))
                .andExpect(status().is4xxClientError());

        // Confirmações e atualização de doação.
        mvc.perform(patch("/api/v1/donations/" + donationId + "/confirmar")
                        .header("Authorization", "Bearer " + tokenInstituicao))
                .andExpect(status().is4xxClientError());
        mvc.perform(post("/api/v1/donations/" + donationId + "/atualizacoes")
                        .header("Authorization", "Bearer " + tokenInstituicao)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .param("mensagem", "Nao deveria publicar"))
                .andExpect(status().is4xxClientError());

        // Confirmação e leitura de pagamento.
        mvc.perform(patch("/api/v1/payments/" + paymentUuid + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenInstituicao))
                .andExpect(status().is4xxClientError());
        mvc.perform(get("/api/v1/payments/" + paymentUuid)
                        .header("Authorization", "Bearer " + tokenInstituicao))
                .andExpect(status().is4xxClientError());

        // Leitura de dados de doadores e ofertas.
        mvc.perform(get("/api/v1/donations/recebidas")
                        .header("Authorization", "Bearer " + tokenInstituicao))
                .andExpect(status().is4xxClientError());
        mvc.perform(get("/api/v1/donations/" + donationId)
                        .header("Authorization", "Bearer " + tokenInstituicao))
                .andExpect(status().is4xxClientError());
        mvc.perform(get("/api/v1/ofertas/reservadas")
                        .header("Authorization", "Bearer " + tokenInstituicao))
                .andExpect(status().is4xxClientError());
    }
}
