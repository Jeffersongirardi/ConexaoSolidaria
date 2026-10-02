package com.conexoessolidarias.api;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FluxoGuardsApiTest {

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

    private long criarCampanha(String tokenInst) throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Guards","descricao":"Teste de guards",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media"}"""))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private String prepararInstituicaoAprovada(String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Guards\",\"email\":\"" + email + "\",\"senha\":\"senha123\","
                                + "\"cnpj\":\"11.222.333/0001-81\",\"razaoSocial\":\"Inst Guards\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile p = profileRepository.findAll().stream()
                .filter(x -> x.getUser().getEmail().equals(email)).findFirst().orElseThrow();
        p.setAprovado(true);
        profileRepository.save(p);
        return token(email, "senha123");
    }

    private String prepararDoador(String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Guards\",\"email\":\"" + email + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated());
        return token(email, "senha123");
    }

    @Test
    void confirmarDuplicado_ehRejeitado() throws Exception {
        String tokenInst = prepararInstituicaoAprovada("guards1@teste.org");
        String tokenDoador = prepararDoador("doador1@teste.org");
        long campaignId = criarCampanha(tokenInst);

        MvcResult doacao = mvc.perform(post("/api/v1/donations")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"item\":\"Feijão\",\"quantidade\":\"5 kg\"}"))
                .andExpect(status().isCreated()).andReturn();
        long donationId = objectMapper.readTree(doacao.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(patch("/api/v1/donations/" + donationId + "/confirmar")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/v1/donations/" + donationId + "/confirmar")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void adminCancelaRecebido_decrementaProgresso() throws Exception {
        String tokenInst = prepararInstituicaoAprovada("guards2@teste.org");
        String tokenDoador = prepararDoador("doador2@teste.org");
        String tokenAdmin = token("admin@conexoessolidarias.org", "admin123");
        long campaignId = criarCampanha(tokenInst);

        MvcResult doacao = mvc.perform(post("/api/v1/donations")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"item\":\"Arroz\",\"quantidade\":\"2 kg\"}"))
                .andExpect(status().isCreated()).andReturn();
        long donationId = objectMapper.readTree(doacao.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(patch("/api/v1/donations/" + donationId + "/confirmar")
                        .header("Authorization", "Bearer " + tokenInst))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/v1/admin/doacoes/" + donationId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"teste\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelado"));

        mvc.perform(get("/api/v1/campaigns/" + campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progresso").value(0));
    }

    @Test
    void oferta_reivindicarDuasVezes_segundaFalha() throws Exception {
        String tokenInst = prepararInstituicaoAprovada("guards3@teste.org");
        String tokenDoador = prepararDoador("doador3@teste.org");
        String tokenAdmin = token("admin@conexoessolidarias.org", "admin123");

        MvcResult oferta = mvc.perform(post("/api/v1/ofertas")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Sofá","descricao":"Sofá 3 lugares em bom estado",
                                "categoria":"movel","cidade":"Curitiba","disponivelAte":"2030-01-01"}"""))
                .andExpect(status().isCreated()).andReturn();
        long ofertaId = objectMapper.readTree(oferta.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(patch("/api/v1/admin/ofertas/" + ofertaId + "/aprovar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/ofertas/" + ofertaId + "/reivindicar")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("reservada"));

        mvc.perform(post("/api/v1/ofertas/" + ofertaId + "/reivindicar")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void oferta_editadaAposRecusa_voltaParaFila() throws Exception {
        String tokenDoador = prepararDoador("doador4@teste.org");
        String tokenAdmin = token("admin@conexoessolidarias.org", "admin123");

        MvcResult oferta = mvc.perform(post("/api/v1/ofertas")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Mesa","descricao":"Mesa de madeira",
                                "categoria":"movel","cidade":"Curitiba","disponivelAte":"2030-01-01"}"""))
                .andExpect(status().isCreated()).andReturn();
        long ofertaId = objectMapper.readTree(oferta.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(patch("/api/v1/admin/ofertas/" + ofertaId + "/recusar")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"foto ruim\"}"))
                .andExpect(status().isOk());

        mvc.perform(put("/api/v1/ofertas/" + ofertaId)
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Mesa","descricao":"Mesa de madeira maciça, fotos novas",
                                "categoria":"movel","cidade":"Curitiba","disponivelAte":"2030-01-01"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprovado").value(false));
    }
}
