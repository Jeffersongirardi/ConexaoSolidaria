package com.conexoessolidarias.api;

import com.conexoessolidarias.repository.InstitutionProfileRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P0.8: campanha sem vínculos pode ser excluída; com doações/pagamentos
 * retorna 409 pedindo para pausar (protege as FKs e a expectativa da UI).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CampanhaExclusaoApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InstitutionProfileRepository profileRepository;

    private String tokenInst;
    private String tokenDoador;

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
        String emailInst = "exc-inst-" + sufixo + "@teste.org";
        String emailDoador = "exc-doador-" + sufixo + "@teste.org";
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));

        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Exclusao\",\"email\":\"" + emailInst + "\",\"senha\":\"senha123\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Exclusao\"}"))
                .andExpect(status().isCreated());
        var profile = profileRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(emailInst)).findFirst().orElseThrow();
        profile.setAprovado(true);
        profileRepository.save(profile);
        tokenInst = token(emailInst, "senha123");

        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Exclusao\",\"email\":\"" + emailDoador + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated());
        tokenDoador = token(emailDoador, "senha123");

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/institutions/minha")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pixKey\":\"" + emailInst + "\"}"))
                .andExpect(status().isOk());
    }

    private long criarCampanha(boolean aceitaFinanceiro) throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Exclusao\",\"descricao\":\"Teste P0.8\","
                                + "\"categoria\":\"alimento\",\"quantidadeAlvo\":\"10\",\"urgencia\":\"media\","
                                + "\"aceitaFinanceiro\":" + aceitaFinanceiro + "}"))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void campanhaVaziaPodeSerRemovida() throws Exception {
        long id = criarCampanha(false);
        mvc.perform(delete("/api/v1/campaigns/" + id)
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/campaigns/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void campanhaComDoacaoNaoPodeSerRemovida() throws Exception {
        long id = criarCampanha(false);
        mvc.perform(post("/api/v1/donations")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + id + ",\"item\":\"Arroz\",\"quantidade\":\"1\"}"))
                .andExpect(status().isCreated());

        mvc.perform(delete("/api/v1/campaigns/" + id)
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Pause a campanha")));

        mvc.perform(get("/api/v1/campaigns/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void campanhaComPagamentoNaoPodeSerRemovida() throws Exception {
        long id = criarCampanha(true);
        mvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + id + ",\"valor\":10.00,\"metodo\":\"pix\"}"))
                .andExpect(status().isCreated());

        mvc.perform(delete("/api/v1/campaigns/" + id)
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isConflict());
    }
}
