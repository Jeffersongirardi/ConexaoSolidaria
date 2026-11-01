package com.conexoessolidarias.api;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.User;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P1.1: eventos sem notificação passam a avisar as partes certas
 * (admin para instituição/oferta/contato; instituição para aprovação/cancelamentos).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificacoesApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InstitutionProfileRepository profileRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    private String tokenAdmin;
    private String tokenDoador;
    private String tokenInst;
    private long adminId;
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

    private long naoLidas(long userId) {
        return notificationRepository.countByUserIdAndLidaFalse(userId);
    }

    @BeforeAll
    void prepararCenario() throws Exception {
        User admin = userRepository.findByEmail("admin@conexoessolidarias.org").orElseThrow();
        adminId = admin.getId();
        tokenAdmin = token("admin@conexoessolidarias.org", "admin123");

        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        String emailDoador = "notif-doador-" + sufixo + "@teste.org";
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Notif\",\"email\":\"" + emailDoador + "\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isCreated());
        tokenDoador = token(emailDoador, "SenhaForte2026");

        String emailInst = "notif-inst-" + sufixo + "@teste.org";
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Notif\",\"email\":\"" + emailInst + "\",\"senha\":\"SenhaForte2026\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Notif\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile profile = profileRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(emailInst)).findFirst().orElseThrow();
        profile.setAprovado(true);
        profileRepository.save(profile);
        instUserId = profile.getUser().getId();
        tokenInst = token(emailInst, "SenhaForte2026");

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/institutions/minha")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pixKey\":\"" + emailInst + "\"}"))
                .andExpect(status().isOk());

        MvcResult campanha = mvc.perform(post("/api/v1/campaigns")
                        .header("Authorization", "Bearer " + tokenInst)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Campanha Notif","descricao":"Teste P1.1",
                                "categoria":"alimento","quantidadeAlvo":"10","urgencia":"media",
                                "aceitaFinanceiro":true}"""))
                .andExpect(status().isCreated()).andReturn();
        campaignId = objectMapper.readTree(campanha.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void adminEInstituicaoRecebemOsEventosEsperados() throws Exception {
        long adminAntes = naoLidas(adminId);

        // Contato público avisa admin.
        mvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Visitante\",\"email\":\"visitante@teste.org\","
                                + "\"assunto\":\"Dúvida\",\"mensagem\":\"Como doar?\"}"))
                .andExpect(status().isCreated());

        // Nova instituição aguardando aprovação avisa admin.
        String emailNovaInst = "notif-nova-" + UUID.randomUUID().toString().substring(0, 8) + "@teste.org";
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));
        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nova Notif\",\"email\":\"" + emailNovaInst + "\",\"senha\":\"SenhaForte2026\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Nova Notif\"}"))
                .andExpect(status().isCreated());

        // Nova oferta avisa admin.
        mvc.perform(post("/api/v1/ofertas")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Mesa Notif","descricao":"Mesa em bom estado",
                                "categoria":"movel","cidade":"Curitiba","disponivelAte":"2035-01-01"}"""))
                .andExpect(status().isCreated());

        assertTrue(naoLidas(adminId) >= adminAntes + 3, "Admin deve receber contato, instituição e oferta");

        // Aprovação avisa a instituição.
        InstitutionProfile nova = profileRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(emailNovaInst)).findFirst().orElseThrow();
        long novaUserId = nova.getUser().getId();
        long novaAntes = naoLidas(novaUserId);
        mvc.perform(patch("/api/v1/admin/institutions/" + nova.getId() + "/aprovar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
        assertTrue(naoLidas(novaUserId) > novaAntes, "Instituição deve ser avisada da aprovação");

        // Cancelamento de doação e criação de Pix avisam a instituição.
        long instAntes = naoLidas(instUserId);
        MvcResult doacao = mvc.perform(post("/api/v1/donations")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"item\":\"Arroz\",\"quantidade\":\"1\"}"))
                .andExpect(status().isCreated()).andReturn();
        long doacaoId = objectMapper.readTree(doacao.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(patch("/api/v1/donations/" + doacaoId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":" + campaignId + ",\"valor\":15.00,\"metodo\":\"pix\"}"))
                .andExpect(status().isCreated());

        assertTrue(naoLidas(instUserId) >= instAntes + 3,
                "Instituição deve receber nova doação, cancelamento e novo Pix");
    }
}
