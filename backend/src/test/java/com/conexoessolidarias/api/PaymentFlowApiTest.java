package com.conexoessolidarias.api;

import com.conexoessolidarias.model.InstitutionProfile;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentFlowApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InstitutionProfileRepository profileRepository;

    private String tokenInst;
    private String tokenDoador;
    private long campaignId;

    private String login(String email, String senha) throws Exception {
        MvcResult login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    /**
     * Fixture única por classe (fora da transação dos testes) para não
     * estourar o RateLimitFilter (20 req/min em /auth/*) quando a suíte
     * completa roda no mesmo contexto Spring.
     */
    @BeforeAll
    void setup() throws Exception {
        // Sufixo único: @BeforeAll commita de verdade (fora da transação dos
        // testes), então emails fixos quebrariam reexecuções contra o H2 em arquivo.
        String tag = java.util.UUID.randomUUID().toString().substring(0, 8);
        String emailInst = "paginst-" + tag + "@teste.org";
        String emailDoador = "pagdoador-" + tag + "@teste.org";
        String cnpj = String.format("%02d.%03d.%03d/0001-%02d",
                10 + (int) (Math.random() * 89),
                (int) (Math.random() * 1000),
                (int) (Math.random() * 1000),
                (int) (Math.random() * 100));
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Pag\",\"email\":\"" + emailInst + "\",\"senha\":\"senha123\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Pag\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile p = profileRepository.findAll().stream()
                .filter(x -> x.getUser().getEmail().equals(emailInst))
                .findFirst().orElseThrow();
        p.setAprovado(true);
        profileRepository.save(p);

        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Pag\",\"email\":\"" + emailDoador + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated());

        tokenInst = login(emailInst, "senha123");
        tokenDoador = login(emailDoador, "senha123");

        MvcResult r = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Ajuda Pix","descricao":"Aceita valor",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media",
                                "aceitaFinanceiro":true}"""))
                .andExpect(status().isCreated()).andReturn();
        campaignId = objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private String criarPagamento() throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"valor\":50.00,\"metodo\":\"pix\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pendente"))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("uuid").asText();
    }

    @Test
    void fluxoFeliz_pendenteConfirmadoRecebido() throws Exception {
        String uuid = criarPagamento();

        mvc.perform(post("/api/v1/payments/" + uuid + "/confirmar-pix")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("confirmado"));

        // comprovante liberado a partir de confirmado
        mvc.perform(get("/api/v1/payments/" + uuid + "/comprovante")
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/v1/payments/" + uuid + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("recebido"));

        // idempotência: recebido de novo rejeita
        mvc.perform(patch("/api/v1/payments/" + uuid + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void instituicaoFinalizaPendenteDiretoParaRecebido() throws Exception {
        String uuid = criarPagamento();

        mvc.perform(patch("/api/v1/payments/" + uuid + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("recebido"));
    }

    @Test
    void doadorCancelaPendente_eNaoCancelaConfirmado() throws Exception {
        String uuid = criarPagamento();

        mvc.perform(patch("/api/v1/payments/" + uuid + "/cancelar")
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelado"));

        String uuid2 = criarPagamento();
        mvc.perform(post("/api/v1/payments/" + uuid2 + "/confirmar-pix")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/v1/payments/" + uuid2 + "/cancelar")
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void comprovanteBloqueadoEnquantoPendente() throws Exception {
        String uuid = criarPagamento();

        mvc.perform(get("/api/v1/payments/" + uuid + "/comprovante")
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().is4xxClientError());
    }
}
