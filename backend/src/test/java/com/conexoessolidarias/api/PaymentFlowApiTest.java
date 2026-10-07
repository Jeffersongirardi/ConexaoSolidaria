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

import static org.junit.jupiter.api.Assertions.assertTrue;
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
        String cnpj = cnpjAleatorio();
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

        // Chave PIX válida (o próprio e-mail) — sem ela, criar pagamento pix rejeita (409)
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/institutions/minha")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pixKey\":\"" + emailInst + "\",\"pixTitular\":\"Inst Pag\"}"))
                .andExpect(status().isOk());

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

    @Test
    void pagamentoPix_expoeBrCodeValido() throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"valor\":25.50,\"metodo\":\"pix\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.copiaECola").isNotEmpty())
                .andExpect(jsonPath("$.qrcode").isNotEmpty())
                .andReturn();
        String copiaECola = objectMapper.readTree(r.getResponse().getContentAsString())
                .get("copiaECola").asText();
        assertTrue(copiaECola.startsWith("000201"));
        assertTrue(copiaECola.contains("br.gov.bcb.pix"));
    }

    @Test
    void pagamentoPix_semChave_rejeitado() throws Exception {
        String tag = java.util.UUID.randomUUID().toString().substring(0, 8);
        String email = "semchave-" + tag + "@teste.org";
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sem Chave\",\"email\":\"" + email + "\",\"senha\":\"senha123\","
                                + "\"cnpj\":\"" + cnpjAleatorio() + "\",\"razaoSocial\":\"Sem Chave\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile p = profileRepository.findAll().stream()
                .filter(x -> x.getUser().getEmail().equals(email)).findFirst().orElseThrow();
        p.setAprovado(true);
        profileRepository.save(p);
        String tInst = login(email, "senha123");
        MvcResult camp = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Sem chave","descricao":"Sem pixKey",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media",
                                "aceitaFinanceiro":true}"""))
                .andExpect(status().isCreated()).andReturn();
        long campId = objectMapper.readTree(camp.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campId + ",\"valor\":10.00,\"metodo\":\"pix\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void cadastroComChaveInvalida_rejeitado() throws Exception {
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Chave Ruim\",\"email\":\"chaveruim@teste.org\",\"senha\":\"senha123\","
                                + "\"cnpj\":\"" + cnpjAleatorio() + "\",\"razaoSocial\":\"Chave Ruim\","
                                + "\"pixKey\":\"abc\"}"))
                .andExpect(status().is4xxClientError());
    }

    private static String cnpjAleatorio() {
        return String.format("%02d.%03d.%03d/0001-%02d",
                10 + (int) (Math.random() * 89),
                (int) (Math.random() * 1000),
                (int) (Math.random() * 1000),
                (int) (Math.random() * 100));
    }
}
