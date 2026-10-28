package com.conexoessolidarias.api;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.Oferta;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.OfertaRepository;
import com.conexoessolidarias.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contato/endereço do doador só podem aparecer após a reserva, para a instituição
 * reservante, o doador dono ou o admin. Fixture única (rate limit de /auth/*).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OfertaPrivacidadeApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InstitutionProfileRepository profileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OfertaRepository ofertaRepository;

    private String tokenReservante;
    private String tokenOutra;
    private long ofertaId;

    private String token(String email, String senha) throws Exception {
        MvcResult login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String instituicaoAprovada(String email) throws Exception {
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Oferta\",\"email\":\"" + email + "\",\"senha\":\"senha123\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Oferta\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile p = profileRepository.findAll().stream()
                .filter(x -> x.getUser().getEmail().equals(email)).findFirst().orElseThrow();
        p.setAprovado(true);
        profileRepository.save(p);
        return token(email, "senha123");
    }

    @BeforeAll
    void prepararCenario() throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        tokenReservante = instituicaoAprovada("oferta-a-" + sufixo + "@teste.org");
        tokenOutra = instituicaoAprovada("oferta-b-" + sufixo + "@teste.org");

        User doador = new User("Doador Oferta", "oferta-doador-" + sufixo + "@teste.org", "hash", "doador");
        doador.setWhatsapp("41999998888");
        doador.setAtivo(true);
        doador = userRepository.save(doador);

        Oferta o = new Oferta();
        o.setDoador(doador);
        o.setTitulo("Sofá sigiloso");
        o.setDescricao("Sofá em bom estado");
        o.setCategoria("movel");
        o.setCidade("Curitiba");
        o.setPrecisaColeta(true);
        o.setEnderecoColeta("Rua Sigilosa, 123");
        o.setDisponivelAte(LocalDate.now().plusDays(7));
        o.setStatus(Oferta.DISPONIVEL);
        o.setAprovado(true);
        ofertaId = ofertaRepository.save(o).getId();
    }

    @Test
    void contatoSoAposReservaParaInstituicaoReservante() throws Exception {
        // Lista de ofertas: nunca expõe contato/endereço.
        MvcResult lista = mvc.perform(get("/api/v1/ofertas")
                        .header("Authorization", "Bearer " + tokenOutra))
                .andExpect(status().isOk()).andReturn();
        JsonNode nosso = null;
        for (JsonNode n : objectMapper.readTree(lista.getResponse().getContentAsString())) {
            if (n.get("id").asLong() == ofertaId) nosso = n;
        }
        assertNotNull(nosso, "Oferta deveria aparecer na lista");
        assertTrue(nosso.get("doadorWhatsapp").isNull(), "Lista não pode expor WhatsApp");
        assertTrue(nosso.get("enderecoColeta").isNull(), "Lista não pode expor endereço");

        // Detalhe em disponível: visível, mas sem contato.
        mvc.perform(get("/api/v1/ofertas/" + ofertaId)
                        .header("Authorization", "Bearer " + tokenOutra))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doadorWhatsapp", nullValue()))
                .andExpect(jsonPath("$.enderecoColeta", nullValue()));

        // A instituição reservante reivindica e recebe o contato.
        mvc.perform(post("/api/v1/ofertas/" + ofertaId + "/reivindicar")
                        .header("Authorization", "Bearer " + tokenReservante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doadorWhatsapp").value("41999998888"))
                .andExpect(jsonPath("$.enderecoColeta").value("Rua Sigilosa, 123"));

        // Reservante continua vendo o contato no detalhe.
        mvc.perform(get("/api/v1/ofertas/" + ofertaId)
                        .header("Authorization", "Bearer " + tokenReservante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doadorWhatsapp").value("41999998888"));

        // Outra instituição não abre mais a oferta reservada.
        mvc.perform(get("/api/v1/ofertas/" + ofertaId)
                        .header("Authorization", "Bearer " + tokenOutra))
                .andExpect(status().isForbidden());

        // Nem tenta confirmar recebimento de reserva alheia.
        mvc.perform(post("/api/v1/ofertas/" + ofertaId + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenOutra)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
