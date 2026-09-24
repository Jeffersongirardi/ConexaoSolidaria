package com.conexoessolidarias.api;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CampaignDonationApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InstitutionProfileRepository profileRepository;

    private String token(String email, String senha) throws Exception {
        MvcResult login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private void aprovarInstituicao(String email) {
        InstitutionProfile p = profileRepository.findAll().stream()
                .filter(x -> x.getUser().getEmail().equals(email))
                .findFirst().orElseThrow();
        p.setAprovado(true);
        profileRepository.save(p);
    }

    @Test
    void fluxoCompleto_campanhaDoacaoConfirmacao() throws Exception {
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Inst Teste","email":"inst@teste.org","senha":"senha123",
                                "cnpj":"12.345.678/0001-90","razaoSocial":"Inst Teste Social"}"""))
                .andExpect(status().isCreated());
        aprovarInstituicao("inst@teste.org");
        String tokenInst = token("inst@teste.org", "senha123");

        MvcResult campanha = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Cesta básica","descricao":"Arrecadação de alimentos",
                                "categoria":"alimento","quantidadeAlvo":"100 cestas","urgencia":"alta",
                                "aceitaFinanceiro":true}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Cesta básica"))
                .andReturn();
        long campaignId = objectMapper.readTree(campanha.getResponse().getContentAsString())
                .get("id").asLong();

        mvc.perform(get("/api/v1/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Doador Fluxo","email":"fluxo@teste.org","senha":"senha123"}"""))
                .andExpect(status().isCreated());
        String tokenDoador = token("fluxo@teste.org", "senha123");

        MvcResult doacao = mvc.perform(post("/api/v1/donations")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId
                                + ",\"item\":\"Arroz\",\"quantidade\":\"10 kg\",\"categoria\":\"alimento\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pendente"))
                .andReturn();
        long donationId = objectMapper.readTree(doacao.getResponse().getContentAsString())
                .get("id").asLong();

        mvc.perform(patch("/api/v1/donations/" + donationId + "/confirmar")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("recebido"));

        mvc.perform(get("/api/v1/notifications/nao-lidas")
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void campanhaSemToken_retorna401() throws Exception {
        mvc.perform(post("/api/v1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"X","descricao":"Y","quantidadeAlvo":"1"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void instituicaoNaoAprovada_naoCriaCampanha() throws Exception {
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Inst Nova","email":"nova@teste.org","senha":"senha123",
                                "cnpj":"98.765.432/0001-10","razaoSocial":"Inst Nova Social"}"""))
                .andExpect(status().isCreated());
        String t = token("nova@teste.org", "senha123");
        mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + t)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"X","descricao":"Y","quantidadeAlvo":"1"}"""))
                .andExpect(status().isConflict());
    }
}
