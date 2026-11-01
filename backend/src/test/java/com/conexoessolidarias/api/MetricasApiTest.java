package com.conexoessolidarias.api;

import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P0.9: valor público só conta `recebido`; doadores ignoram pendentes;
 * declaração ("Já paguei") não infla métricas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetricasApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InstitutionProfileRepository profileRepository;

    private String tokenInst;
    private String tokenDoador;
    private long campaignId;
    private long institutionId;

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
        String emailInst = "metrica-inst-" + sufixo + "@teste.org";
        String emailDoador = "metrica-doador-" + sufixo + "@teste.org";
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));

        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Metrica\",\"email\":\"" + emailInst + "\",\"senha\":\"SenhaForte2026\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Metrica\"}"))
                .andExpect(status().isCreated());
        var profile = profileRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(emailInst)).findFirst().orElseThrow();
        profile.setAprovado(true);
        profileRepository.save(profile);
        institutionId = profile.getId();
        tokenInst = token(emailInst, "SenhaForte2026");

        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Metrica\",\"email\":\"" + emailDoador + "\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isCreated());
        tokenDoador = token(emailDoador, "SenhaForte2026");

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/institutions/minha")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pixKey\":\"" + emailInst + "\"}"))
                .andExpect(status().isOk());

        MvcResult campanha = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Metricas P0.9","descricao":"Teste de metricas",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media",
                                "aceitaFinanceiro":true}"""))
                .andExpect(status().isCreated()).andReturn();
        campaignId = objectMapper.readTree(campanha.getResponse().getContentAsString()).get("id").asLong();
    }

    private JsonNode campanha() throws Exception {
        MvcResult r = mvc.perform(get("/api/v1/campaigns/" + campaignId))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString());
    }

    @Test
    void valorEDoadoresSoContamRecebido() throws Exception {
        MvcResult doacao = mvc.perform(post("/api/v1/donations")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"item\":\"Arroz\",\"quantidade\":\"1\"}"))
                .andExpect(status().isCreated()).andReturn();
        long doacaoId = objectMapper.readTree(doacao.getResponse().getContentAsString()).get("id").asLong();

        MvcResult pagamento = mvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"valor\":25.50,\"metodo\":\"pix\"}"))
                .andExpect(status().isCreated()).andReturn();
        String paymentUuid = objectMapper.readTree(pagamento.getResponse().getContentAsString()).get("uuid").asText();

        JsonNode inicial = campanha();
        assertEquals(0, inicial.get("numDoadores").asInt());
        assertEquals(0, inicial.get("valorRecebido").asDouble(), 0.001);
        assertEquals(0, inicial.get("numDoacoesItens").asInt());

        // Declaração do doador ("Já paguei") não infla valor público.
        mvc.perform(post("/api/v1/payments/" + paymentUuid + "/confirmar-pix")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
        JsonNode declarado = campanha();
        assertEquals(1, declarado.get("numDoadores").asInt());
        assertEquals(0, declarado.get("valorRecebido").asDouble(), 0.001);

        // Confirmação da instituição conta valor e doação de item.
        mvc.perform(patch("/api/v1/donations/" + doacaoId + "/confirmar")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/v1/payments/" + paymentUuid + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk());

        JsonNode recebido = campanha();
        assertEquals(1, recebido.get("numDoadores").asInt());
        assertEquals(25.50, recebido.get("valorRecebido").asDouble(), 0.001);
        assertEquals(1, recebido.get("numDoacoesItens").asInt());

        // Página pública da instituição não zera as métricas das campanhas.
        MvcResult inst = mvc.perform(get("/api/v1/institutions/" + institutionId))
                .andExpect(status().isOk()).andReturn();
        JsonNode campanhasInst = objectMapper.readTree(inst.getResponse().getContentAsString()).get("campanhas");
        assertEquals(1, campanhasInst.get(0).get("numDoadores").asInt());
        assertEquals(25.50, campanhasInst.get(0).get("valorRecebido").asDouble(), 0.001);

        // /stats separa as unidades e mantém o total agregado coerente.
        MvcResult stats = mvc.perform(get("/api/v1/stats")).andExpect(status().isOk()).andReturn();
        JsonNode s = objectMapper.readTree(stats.getResponse().getContentAsString());
        assertEquals(s.get("itensRecebidos").asLong() + s.get("pagamentosRecebidos").asLong()
                        + s.get("ofertasEntregues").asLong(),
                s.get("doacoesRecebidas").asLong());
    }
}
