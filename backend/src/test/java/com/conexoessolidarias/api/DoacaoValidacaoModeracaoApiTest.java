package com.conexoessolidarias.api;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P0.7: quantidade numérica (com unidade opcional), coleta exige endereço,
 * instituição pode recusar doação pendente e o admin limpa dataRecebimento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DoacaoValidacaoModeracaoApiTest {

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
        String emailInst = "doacao-inst-" + sufixo + "@teste.org";
        String emailDoador = "doacao-doador-" + sufixo + "@teste.org";
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));

        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Doacao\",\"email\":\"" + emailInst + "\",\"senha\":\"senha123\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Doacao\"}"))
                .andExpect(status().isCreated());
        var profile = profileRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(emailInst)).findFirst().orElseThrow();
        profile.setAprovado(true);
        profileRepository.save(profile);
        tokenInst = token(emailInst, "senha123");

        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Doacao\",\"email\":\"" + emailDoador + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated());
        tokenDoador = token(emailDoador, "senha123");
        doadorId = userRepository.findByEmail(emailDoador).orElseThrow().getId();
        tokenAdmin = token("admin@conexoessolidarias.org", "admin123");

        MvcResult campanha = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Doacao P0.7","descricao":"Teste de validacoes",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media"}"""))
                .andExpect(status().isCreated()).andReturn();
        campaignId = objectMapper.readTree(campanha.getResponse().getContentAsString()).get("id").asLong();
    }

    private MvcResult criarDoacao(String quantidade, String extra) throws Exception {
        return mvc.perform(post("/api/v1/donations")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"item\":\"Arroz\","
                                + "\"quantidade\":\"" + quantidade + "\"" + extra + "}"))
                .andReturn();
    }

    private long criarDoacaoValida(String quantidade) throws Exception {
        MvcResult r = criarDoacao(quantidade, "");
        assertEquals(201, r.getResponse().getStatus(), r.getResponse().getContentAsString());
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private int progresso() {
        return campaignRepository.findById(campaignId).orElseThrow().getProgresso();
    }

    private long naoLidasDoador() {
        return notificationRepository.countByUserIdAndLidaFalse(doadorId);
    }

    @Test
    void quantidadeInvalidaEhRejeitadaEAceitaUnidade() throws Exception {
        for (String invalida : new String[]{"0", "-5", "abc", "999999"}) {
            assertEquals(400, criarDoacao(invalida, "").getResponse().getStatus(),
                    "Quantidade deveria ser rejeitada: " + invalida);
        }
        criarDoacaoValida("10");
        criarDoacaoValida("5 kg");
    }

    @Test
    void coletaSemEnderecoEhRejeitada() throws Exception {
        assertEquals(400, criarDoacao("2", ",\"precisaColeta\":true").getResponse().getStatus());
        assertEquals(201, criarDoacao("2", ",\"precisaColeta\":true,\"enderecoColeta\":\"Rua A, 10\"").getResponse().getStatus());
    }

    @Test
    void instituicaoRecusaDoacaoPendenteComMotivo() throws Exception {
        long doacaoId = criarDoacaoValida("3");
        long notificacoesAntes = naoLidasDoador();

        mvc.perform(patch("/api/v1/donations/" + doacaoId + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/api/v1/donations/" + doacaoId + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Sem estrutura para armazenar\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("rejeitado"))
                .andExpect(jsonPath("$.motivoRejeicao").value("Sem estrutura para armazenar"));

        assertTrue(naoLidasDoador() > notificacoesAntes, "Doador deve ser avisado da recusa");

        mvc.perform(patch("/api/v1/donations/" + doacaoId + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"de novo\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void adminCancelaRecebidaELimpaDataRecebimento() throws Exception {
        long doacaoId = criarDoacaoValida("1");
        int progressoAntes = progresso();

        MvcResult confirmada = mvc.perform(patch("/api/v1/donations/" + doacaoId + "/confirmar")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataRecebimento").isNotEmpty())
                .andReturn();
        assertNotNull(objectMapper.readTree(confirmada.getResponse().getContentAsString())
                .get("dataRecebimento").asText());
        assertEquals(progressoAntes + 10, progresso());

        mvc.perform(patch("/api/v1/admin/doacoes/" + doacaoId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Teste de moderacao\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelado"))
                .andExpect(jsonPath("$.dataRecebimento").value(org.hamcrest.Matchers.nullValue()));

        assertEquals(progressoAntes, progresso());
    }
}
