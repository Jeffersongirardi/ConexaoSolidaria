package com.conexoessolidarias.api;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.conexoessolidarias.service.EmailService;
import com.conexoessolidarias.repository.UserRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
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

import static org.hamcrest.Matchers.nullValue;
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

    @MockBean private EmailService emailService;
    @Autowired private UserRepository userRepository;
    private String emailInst;
    private String emailDoador;
    private String tokenOutroDoador;
    private String tokenOutraInst;
    private String tokenAdmin;
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
        emailInst = "paginst-" + tag + "@teste.org";
        emailDoador = "pagdoador-" + tag + "@teste.org";
        String cnpj = cnpjAleatorio();
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Pag\",\"email\":\"" + emailInst + "\",\"senha\":\"SenhaForte2026\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Pag\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile p = profileRepository.findAll().stream()
                .filter(x -> x.getUser().getEmail().equals(emailInst))
                .findFirst().orElseThrow();
        p.setAprovado(true);
        profileRepository.save(p);

        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Pag\",\"email\":\"" + emailDoador + "\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isCreated());

        tokenOutroDoador = cadastrarDoador("outro-" + tag + "@teste.org", false);
        tokenAdmin = cadastrarDoador("admin-" + tag + "@teste.org", true);
        String outraInst = "outrainst-" + tag + "@teste.org";
        mvc.perform(post("/api/v1/auth/register/instituicao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of(
                        "nome", "Outra", "email", outraInst, "senha", "SenhaForte2026",
                        "cnpj", cnpjAleatorio(), "razaoSocial", "Outra"))))
                .andExpect(status().isCreated());
        var outroPerfil = profileRepository.findAll().stream()
                .filter(x -> x.getUser().getEmail().equals(outraInst)).findFirst().orElseThrow();
        outroPerfil.setAprovado(true);
        profileRepository.save(outroPerfil);
        tokenOutraInst = login(outraInst, "SenhaForte2026");
        tokenInst = login(emailInst, "SenhaForte2026");
        tokenDoador = login(emailDoador, "SenhaForte2026");

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
    void somentePixEhAceito() throws Exception {
        for (String metodo : java.util.List.of("cartao", "transferencia")) {
            mvc.perform(post("/api/v1/payments")
                            .header("Authorization", "Bearer " + tokenDoador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"campaignId\":" + campaignId + ",\"valor\":10.00,\"metodo\":\"" + metodo + "\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void cancelamentoInvalidaBrCode() throws Exception {
        String uuid = criarPagamento();
        mvc.perform(patch("/api/v1/payments/" + uuid + "/cancelar")
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelado"))
                .andExpect(jsonPath("$.copiaECola").value(nullValue()))
                .andExpect(jsonPath("$.qrcode").value(nullValue()));
    }

    @Test
    void pagamentoPix_semChave_rejeitado() throws Exception {
        String tag = java.util.UUID.randomUUID().toString().substring(0, 8);
        String email = "semchave-" + tag + "@teste.org";
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sem Chave\",\"email\":\"" + email + "\",\"senha\":\"SenhaForte2026\","
                                + "\"cnpj\":\"" + cnpjAleatorio() + "\",\"razaoSocial\":\"Sem Chave\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile p = profileRepository.findAll().stream()
                .filter(x -> x.getUser().getEmail().equals(email)).findFirst().orElseThrow();
        p.setAprovado(true);
        profileRepository.save(p);
        String tInst = login(email, "SenhaForte2026");
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
                        .content("{\"nome\":\"Chave Ruim\",\"email\":\"chaveruim@teste.org\",\"senha\":\"SenhaForte2026\","
                                + "\"cnpj\":\"" + cnpjAleatorio() + "\",\"razaoSocial\":\"Chave Ruim\","
                                + "\"pixKey\":\"abc\"}"))
                .andExpect(status().is4xxClientError());
    }


    private String cadastrarDoador(String email, boolean admin) throws Exception {
        mvc.perform(post("/api/v1/auth/register/doador")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of(
                        "nome", "Teste", "email", email, "senha", "SenhaForte2026"))))
                .andExpect(status().isCreated());
        if (admin) {
            var user = userRepository.findByEmail(email).orElseThrow();
            user.setTipo("admin");
            userRepository.save(user);
        }
        return login(email, "SenhaForte2026");
    }

    @Test
    void comprovanteEDetalhesRespeitamParticipantes() throws Exception {
        String uuid = criarPagamento();
        mvc.perform(post("/api/v1/payments/" + uuid + "/confirmar-pix")
                .header("Authorization", "Bearer " + tokenDoador)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        for (String sufixo : java.util.List.of("", "/comprovante")) {
            String url = "/api/v1/payments/" + uuid + sufixo;
            for (String token : java.util.List.of(tokenDoador, tokenInst, tokenAdmin)) {
                mvc.perform(get(url).header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk());
            }
            for (String token : java.util.List.of(tokenOutroDoador, tokenOutraInst)) {
                mvc.perform(get(url).header("Authorization", "Bearer " + token))
                        .andExpect(status().isForbidden());
            }
            mvc.perform(get(url)).andExpect(status().is4xxClientError());
        }
    }

    @Test
    void eventosPixEnviamAoParticipanteCorretoSemDuplicarRecebimento() throws Exception {
        clearInvocations(emailService);
        String uuid = criarPagamento();
        verify(emailService).evento(eq(emailInst), eq("Novo Pix iniciado"), anyString());
        mvc.perform(post("/api/v1/payments/" + uuid + "/confirmar-pix")
                .header("Authorization", "Bearer " + tokenDoador)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        verify(emailService).evento(eq(emailInst), eq("Pix declarado pelo doador"), anyString());
        mvc.perform(patch("/api/v1/payments/" + uuid + "/confirmar-recebimento")
                .header("Authorization", "Bearer " + tokenInst)).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/payments/" + uuid + "/confirmar-recebimento")
                .header("Authorization", "Bearer " + tokenInst)).andExpect(status().is4xxClientError());
        verify(emailService, times(1)).evento(eq(emailDoador), eq("Pix recebido!"), anyString());
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void cancelamentoERejeicaoNotificamDestinatariosCorretos() throws Exception {
        String cancelado = criarPagamento();
        clearInvocations(emailService);
        mvc.perform(patch("/api/v1/payments/" + cancelado + "/cancelar")
                .header("Authorization", "Bearer " + tokenDoador)).andExpect(status().isOk());
        verify(emailService).evento(eq(emailInst), eq("Pix cancelado pelo doador"), anyString());
        verifyNoMoreInteractions(emailService);
        String rejeitado = criarPagamento();
        clearInvocations(emailService);
        mvc.perform(patch("/api/v1/payments/" + rejeitado + "/rejeitar")
                .header("Authorization", "Bearer " + tokenInst)
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Não consta no extrato\"}"))
                .andExpect(status().isOk());
        verify(emailService).evento(eq(emailDoador), eq("Pix não identificado"), contains("Não consta no extrato"));
        verifyNoMoreInteractions(emailService);
    }

    private static String cnpjAleatorio() {
        return String.format("%02d.%03d.%03d/0001-%02d",
                10 + (int) (Math.random() * 89),
                (int) (Math.random() * 1000),
                (int) (Math.random() * 1000),
                (int) (Math.random() * 100));
    }
}
