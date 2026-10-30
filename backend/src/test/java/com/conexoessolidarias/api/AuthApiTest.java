package com.conexoessolidarias.api;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fluxoDoador_registroLoginPerfil() throws Exception {
        String registro = """
                {"nome":"Teste Doador","email":"doador@teste.org","senha":"SenhaForte2026"}""";
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("doador@teste.org"))
                .andExpect(jsonPath("$.tipo").value("doador"));

        String login = """
                {"email":"doador@teste.org","senha":"SenhaForte2026"}""";
        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();
        String access = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();

        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Teste Doador"));

        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroDuplicado_retorna409() throws Exception {
        String registro = """
                {"nome":"Dup","email":"dup@teste.org","senha":"SenhaForte2026"}""";
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isConflict());
    }

    @Test
    void refreshToken_giraNovoAccessToken() throws Exception {
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Refresh","email":"refresh@teste.org","senha":"SenhaForte2026"}"""))
                .andExpect(status().isCreated());

        MvcResult login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"refresh@teste.org","senha":"SenhaForte2026"}"""))
                .andExpect(status().isOk()).andReturn();
        JsonNode body = objectMapper.readTree(login.getResponse().getContentAsString());

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + body.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void registroComSenhaFraca_retorna400() throws Exception {
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Fraco Curta\",\"email\":\"fraco-curta@teste.org\",\"senha\":\"curta123\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Fraco Comum\",\"email\":\"fraco-comum@teste.org\",\"senha\":\"1234567890\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void trocaDeSenhaFraca_retorna400() throws Exception {
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Troca Senha\",\"email\":\"troca-senha@teste.org\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isCreated());

        MvcResult login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"troca-senha@teste.org\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isOk()).andReturn();
        String access = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();

        mvc.perform(post("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"SenhaForte2026\",\"novaSenha\":\"1234567890\",\"confirmacao\":\"1234567890\"}"))
                .andExpect(status().isBadRequest());
    }
}
