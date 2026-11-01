package com.conexoessolidarias.api;

import com.conexoessolidarias.model.PasswordResetToken;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.PasswordResetTokenRepository;
import com.conexoessolidarias.repository.UserRepository;
import com.conexoessolidarias.service.PasswordResetService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * S5: token de reset é hasheado, expira em 30 min, é uso único e limitado a 3/h;
 * redefinir senha invalida as sessões antigas (token_version).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResetSenhaApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private PasswordResetService passwordResetService;

    private String email;
    private long userId;
    private String accessAntigo;
    private String refreshAntigo;

    private String token(String email, String senha) throws Exception {
        MvcResult login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andReturn();
        assertEquals(200, login.getResponse().getStatus(), login.getResponse().getContentAsString());
        return objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @BeforeAll
    void prepararCenario() throws Exception {
        email = "reset-" + UUID.randomUUID().toString().substring(0, 8) + "@teste.org";
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Reset Teste\",\"email\":\"" + email + "\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isCreated());
        userId = userRepository.findByEmail(email).orElseThrow().getId();
        MvcResult login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isOk()).andReturn();
        var body = objectMapper.readTree(login.getResponse().getContentAsString());
        accessAntigo = body.get("accessToken").asText();
        refreshAntigo = body.get("refreshToken").asText();
    }

    @Test
    void resetEndurecidoInvalidaSessoesELimitaPedidos() throws Exception {
        User user = userRepository.findByEmail(email).orElseThrow();
        String raw = passwordResetService.solicitar(user).orElseThrow();

        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + raw + "\",\"senha\":\"NovaSenhaForte2026\","
                                + "\"confirmacao\":\"NovaSenhaForte2026\"}"))
                .andExpect(status().isOk());

        // Sessão antiga cai (token_version).
        mvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessAntigo))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshAntigo + "\"}"))
                .andExpect(status().isBadRequest());

        // Senha antiga não entra; nova entra.
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isUnauthorized());
        token(email, "NovaSenhaForte2026");

        // Token é de uso único.
        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + raw + "\",\"senha\":\"OutraSenhaForte2026\","
                                + "\"confirmacao\":\"OutraSenhaForte2026\"}"))
                .andExpect(status().isBadRequest());

        // Limite de 3 pedidos por hora (o token acima já conta).
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\"}"))
                    .andExpect(status().isOk());
        }
        long recentes = tokenRepository.countByUserIdAndCreatedAtAfter(
                userId, LocalDateTime.now().minusHours(1));
        assertEquals(3, recentes, "não pode emitir mais de 3 tokens por hora");

        // Token expirado é rejeitado (valor único para suportar reexecuções no H2 em arquivo).
        String rawExpirado = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "");
        PasswordResetToken expirado = new PasswordResetToken(
                PasswordResetService.hash(rawExpirado), user);
        expirado.setExpiryDate(LocalDateTime.now().minusMinutes(5));
        tokenRepository.save(expirado);
        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + rawExpirado + "\",\"senha\":\"OutraSenhaForte2026\","
                                + "\"confirmacao\":\"OutraSenhaForte2026\"}"))
                .andExpect(status().isBadRequest());

        assertTrue(recentes <= PasswordResetService.MAX_POR_HORA);
    }
}
