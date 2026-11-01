package com.conexoessolidarias.api;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.EmailVerificationTokenRepository;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.UserRepository;
import com.conexoessolidarias.service.EmailVerificationService;
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

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * S2: com a flag ligada, doar/ofertar exige e-mail verificado; o token é de uso
 * único e o reenvio é limitado a 3/hora.
 */
@SpringBootTest(properties = "app.security.require-email-verification=true")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VerificacaoEmailApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InstitutionProfileRepository profileRepository;
    @Autowired
    private EmailVerificationTokenRepository tokenRepository;
    @Autowired
    private EmailVerificationService emailVerificationService;

    private String emailDoador;
    private long doadorId;
    private String tokenDoador;
    private String tokenInst;
    private long instUserId;
    private long campaignId;

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
        emailDoador = "verif-doador-" + sufixo + "@teste.org";
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Verif\",\"email\":\"" + emailDoador + "\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isCreated());
        doadorId = userRepository.findByEmail(emailDoador).orElseThrow().getId();
        tokenDoador = token(emailDoador, "SenhaForte2026");

        String emailInst = "verif-inst-" + sufixo + "@teste.org";
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Verif\",\"email\":\"" + emailInst + "\",\"senha\":\"SenhaForte2026\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Verif\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile profile = profileRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(emailInst)).findFirst().orElseThrow();
        profile.setAprovado(true);
        profileRepository.save(profile);
        instUserId = profile.getUser().getId();
        tokenInst = token(emailInst, "SenhaForte2026");
        MvcResult campanha = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Verificacao","descricao":"Teste S2",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media"}"""))
                .andExpect(status().isCreated()).andReturn();
        campaignId = objectMapper.readTree(campanha.getResponse().getContentAsString()).get("id").asLong();
    }

    private MvcResult criarDoacao() throws Exception {
        return mvc.perform(post("/api/v1/donations")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"item\":\"Arroz\",\"quantidade\":\"1\"}"))
                .andReturn();
    }

    @Test
    void acoesSensiveisExigemEmailVerificado() throws Exception {
        assertEquals(403, criarDoacao().getResponse().getStatus(),
                "Doador não verificado não pode doar");

        User doador = userRepository.findById(doadorId).orElseThrow();
        String raw = emailVerificationService.gerar(doador).orElseThrow();

        mvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + raw + "\"}"))
                .andExpect(status().isOk());

        assertEquals(201, criarDoacao().getResponse().getStatus(), "Após verificar, a doação é liberada");

        mvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + raw + "\"}"))
                .andExpect(status().isBadRequest());

        // Reenvio: no máximo 3 tokens por hora (a instituição já tem o token do cadastro).
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/v1/auth/resend-verification")
                            .header("Authorization", "Bearer " + tokenInst)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }
        long tokens = tokenRepository.countByUserIdAndCreatedAtAfter(
                instUserId, LocalDateTime.now().minusHours(1));
        assertEquals(3, tokens);
        assertTrue(tokens <= EmailVerificationService.MAX_POR_HORA);
    }
}
