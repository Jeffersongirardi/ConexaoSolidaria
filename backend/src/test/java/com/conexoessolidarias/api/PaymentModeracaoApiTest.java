package com.conexoessolidarias.api;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.repository.CampaignRepository;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.NotificationRepository;
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

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P0.6: instituição rejeita Pix, admin cancela e data de recebimento é separada
 * da declaração do doador. Rejeitado/cancelado não contam no progresso.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentModeracaoApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InstitutionProfileRepository profileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    private String tokenInst;
    private String tokenDoador;
    private String tokenAdmin;
    private long campaignId;
    private long doadorId;

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
        String emailInst = "modpag-inst-" + sufixo + "@teste.org";
        String emailDoador = "modpag-doador-" + sufixo + "@teste.org";
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));

        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst ModPag\",\"email\":\"" + emailInst + "\",\"senha\":\"senha123\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst ModPag\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile profile = profileRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(emailInst)).findFirst().orElseThrow();
        profile.setAprovado(true);
        profileRepository.save(profile);
        tokenInst = token(emailInst, "senha123");

        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador ModPag\",\"email\":\"" + emailDoador + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated());
        tokenDoador = token(emailDoador, "senha123");
        doadorId = userRepository.findByEmail(emailDoador).orElseThrow().getId();
        tokenAdmin = token("admin@conexoessolidarias.org", "admin123");

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/institutions/minha")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pixKey\":\"" + emailInst + "\",\"pixTitular\":\"Inst ModPag\"}"))
                .andExpect(status().isOk());

        MvcResult campanha = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Pix Moderacao","descricao":"Teste P0.6",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media",
                                "aceitaFinanceiro":true}"""))
                .andExpect(status().isCreated()).andReturn();
        campaignId = objectMapper.readTree(campanha.getResponse().getContentAsString()).get("id").asLong();
    }

    private String criarPagamento() throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"valor\":30.00,\"metodo\":\"pix\"}"))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("uuid").asText();
    }

    private String confirmarPix(String uuid) throws Exception {
        return mvc.perform(post("/api/v1/payments/" + uuid + "/confirmar-pix")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
    }

    private int progresso() {
        return campaignRepository.findById(campaignId).orElseThrow().getProgresso();
    }

    private long naoLidasDoador() {
        return notificationRepository.countByUserIdAndLidaFalse(doadorId);
    }

    @Test
    void instituicaoRejeitaPixDeclaradoEDevolveProgresso() throws Exception {
        String uuid = criarPagamento();
        int antes = progresso();
        confirmarPix(uuid);
        assertEquals(antes + 5, progresso());

        long notificacoesAntes = naoLidasDoador();

        // Motivo é obrigatório.
        mvc.perform(patch("/api/v1/payments/" + uuid + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/api/v1/payments/" + uuid + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Pix não identificado no extrato\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("rejeitado"))
                .andExpect(jsonPath("$.motivoRejeicao").value("Pix não identificado no extrato"))
                .andExpect(jsonPath("$.copiaECola").value(nullValue()));

        assertEquals(antes, progresso(), "Rejeitar confirmado deve devolver o +5 do progresso");
        assertTrue(naoLidasDoador() > notificacoesAntes, "Doador deve ser notificado da rejeição");

        // Não pode rejeitar duas vezes.
        mvc.perform(patch("/api/v1/payments/" + uuid + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"de novo\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void adminCancelaPagamentoEListaPagamentos() throws Exception {
        String uuid = criarPagamento();
        int antes = progresso();
        confirmarPix(uuid);
        assertEquals(antes + 5, progresso());

        mvc.perform(get("/api/v1/admin/payments")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").exists());

        mvc.perform(patch("/api/v1/admin/payments/" + uuid + "/cancelar")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Suspeita de fraude\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelado"))
                .andExpect(jsonPath("$.motivoCancelamento").value("Suspeita de fraude"))
                .andExpect(jsonPath("$.copiaECola").value(nullValue()));

        assertEquals(antes, progresso());

        // Recebido não pode ser cancelado pelo admin.
        String uuidRecebido = criarPagamento();
        mvc.perform(patch("/api/v1/payments/" + uuidRecebido + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/v1/admin/payments/" + uuidRecebido + "/cancelar")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"teste\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void dataRecebimentoSeparadaDaDeclaracaoDoDoador() throws Exception {
        String uuidPendente = criarPagamento();
        MvcResult recebidoDireto = mvc.perform(patch("/api/v1/payments/" + uuidPendente + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataRecebimento").isNotEmpty())
                .andExpect(jsonPath("$.dataConfirmacao").value(nullValue()))
                .andReturn();
        assertNotNull(objectMapper.readTree(recebidoDireto.getResponse().getContentAsString())
                .get("dataRecebimento").asText());

        String uuidDeclarado = criarPagamento();
        confirmarPix(uuidDeclarado);
        mvc.perform(patch("/api/v1/payments/" + uuidDeclarado + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataRecebimento").isNotEmpty())
                .andExpect(jsonPath("$.dataConfirmacao").isNotEmpty());
    }
}
