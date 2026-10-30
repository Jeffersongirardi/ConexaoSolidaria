package com.conexoessolidarias.api;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.Oferta;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.NotificationRepository;
import com.conexoessolidarias.repository.OfertaRepository;
import com.conexoessolidarias.repository.UserRepository;
import com.conexoessolidarias.service.OfertaExpiracaoService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P0.4: aceite de coleta é obrigatório e persistido; reserva vencida é liberada
 * automaticamente (com notificações); confirmação após o prazo é rejeitada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OfertaPrazoColetaApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OfertaRepository ofertaRepository;
    @Autowired
    private InstitutionProfileRepository profileRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private OfertaExpiracaoService expiracaoService;

    private String tokenDoador;
    private String tokenInstituicao;
    private long ofertaDisponivelId;
    private long ofertaVencidaId;
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
        String emailInst = "prazo-inst-" + sufixo + "@teste.org";
        String cnpj = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));

        mvc.perform(post("/api/v1/auth/register/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inst Prazo\",\"email\":\"" + emailInst + "\",\"senha\":\"SenhaForte2026\","
                                + "\"cnpj\":\"" + cnpj + "\",\"razaoSocial\":\"Inst Prazo\"}"))
                .andExpect(status().isCreated());
        InstitutionProfile profile = profileRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(emailInst)).findFirst().orElseThrow();
        profile.setAprovado(true);
        profileRepository.save(profile);
        tokenInstituicao = token(emailInst, "SenhaForte2026");

        String emailDoador = "prazo-doador-" + sufixo + "@teste.org";
        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Prazo\",\"email\":\"" + emailDoador + "\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isCreated());
        tokenDoador = token(emailDoador, "SenhaForte2026");
        User doador = userRepository.findByEmail(emailDoador).orElseThrow();
        doadorId = doador.getId();

        Oferta disponivel = new Oferta();
        disponivel.setDoador(doador);
        disponivel.setTitulo("Cadeira");
        disponivel.setDescricao("Cadeira de madeira");
        disponivel.setCategoria("movel");
        disponivel.setCidade("Curitiba");
        disponivel.setDisponivelAte(LocalDate.now().plusDays(30));
        disponivel.setStatus(Oferta.DISPONIVEL);
        disponivel.setAprovado(true);
        ofertaDisponivelId = ofertaRepository.save(disponivel).getId();

        Oferta vencida = new Oferta();
        vencida.setDoador(doador);
        vencida.setTitulo("Mesa");
        vencida.setDescricao("Mesa usada");
        vencida.setCategoria("movel");
        vencida.setCidade("Curitiba");
        vencida.setDisponivelAte(LocalDate.now().plusDays(30));
        vencida.setStatus(Oferta.RESERVADA);
        vencida.setAprovado(true);
        vencida.setInstituicao(profile);
        vencida.setReservadaEm(java.time.LocalDateTime.now().minusDays(10));
        vencida.setPrazoColeta(LocalDate.now().minusDays(3));
        vencida.setAceiteColeta(true);
        ofertaVencidaId = ofertaRepository.save(vencida).getId();
    }

    @Test
    void aceiteObrigatorioEExpiracaoDaReserva() throws Exception {
        // Sem aceite explícito não pode reivindicar.
        mvc.perform(post("/api/v1/ofertas/" + ofertaDisponivelId + "/reivindicar")
                        .header("Authorization", "Bearer " + tokenInstituicao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // Com aceite, reserva e persiste o aceite.
        mvc.perform(post("/api/v1/ofertas/" + ofertaDisponivelId + "/reivindicar")
                        .header("Authorization", "Bearer " + tokenInstituicao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aceiteColeta\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("reservada"));
        Oferta reservada = ofertaRepository.findById(ofertaDisponivelId).orElseThrow();
        assertTrue(Boolean.TRUE.equals(reservada.getAceiteColeta()));
        assertFalse(reservada.getPrazoColeta().isBefore(LocalDate.now()));

        // Confirmação depois do prazo é rejeitada.
        mvc.perform(post("/api/v1/ofertas/" + ofertaVencidaId + "/confirmar-recebimento")
                        .header("Authorization", "Bearer " + tokenInstituicao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());

        // Job libera a reserva vencida e notifica o doador.
        long notificacoesAntes = notificationRepository.countByUserIdAndLidaFalse(doadorId);
        expiracaoService.expirarReservasVencidas();

        Oferta liberada = ofertaRepository.findById(ofertaVencidaId).orElseThrow();
        assertEquals(Oferta.DISPONIVEL, liberada.getStatus());
        assertNull(liberada.getInstituicao(), "Instituição deve ser desvinculada");
        assertNull(liberada.getPrazoColeta(), "Prazo deve ser limpo");
        assertFalse(Boolean.TRUE.equals(liberada.getAceiteColeta()));
        assertTrue(notificationRepository.countByUserIdAndLidaFalse(doadorId) > notificacoesAntes,
                "Doador deve ser avisado da expiração");

        // A reserva válida (prazo futuro) não é afetada pelo job.
        assertTrue(Oferta.RESERVADA.equals(ofertaRepository.findById(ofertaDisponivelId).orElseThrow().getStatus()));
    }
}
