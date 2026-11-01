package com.conexoessolidarias.api;

import com.conexoessolidarias.model.Oferta;
import com.conexoessolidarias.model.OfertaImage;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.OfertaImageRepository;
import com.conexoessolidarias.repository.OfertaRepository;
import com.conexoessolidarias.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Editar conteúdo ou fotos de uma oferta aprovada deve devolver a oferta
 * para a fila de moderação. Limite de 5 fotos é validado no backend.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OfertaEdicaoModeracaoApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OfertaRepository ofertaRepository;
    @Autowired
    private OfertaImageRepository imageRepository;

    private String tokenDoador;
    private long ofertaId;

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
        String email = "oferta-edicao-" + sufixo + "@teste.org";

        mvc.perform(post("/api/v1/auth/register/doador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Doador Edicao\",\"email\":\"" + email + "\",\"senha\":\"SenhaForte2026\"}"))
                .andExpect(status().isCreated());
        tokenDoador = token(email, "SenhaForte2026");

        User doador = userRepository.findByEmail(email).orElseThrow();
        Oferta o = new Oferta();
        o.setDoador(doador);
        o.setTitulo("Estante");
        o.setDescricao("Estante de madeira em bom estado");
        o.setCategoria("movel");
        o.setCidade("Curitiba");
        o.setDisponivelAte(LocalDate.now().plusDays(30));
        o.setStatus(Oferta.DISPONIVEL);
        o.setAprovado(true);
        ofertaId = ofertaRepository.save(o).getId();
    }

    private void reaprovar() {
        Oferta o = ofertaRepository.findById(ofertaId).orElseThrow();
        o.setAprovado(true);
        o.setMotivoRecusa(null);
        ofertaRepository.save(o);
    }

    private boolean aprovada() {
        return Boolean.TRUE.equals(ofertaRepository.findById(ofertaId).orElseThrow().getAprovado());
    }

    private MockMultipartFile foto(String nome) {
        return new MockMultipartFile("imagem", nome, "image/png", new byte[]{1, 2, 3, 4});
    }

    @Test
    void editarConteudoOuFotoExigeNovaModeracaoELimitaCincoFotos() throws Exception {
        // 1) Editar conteúdo de oferta já aprovada → volta para moderação.
        mvc.perform(put("/api/v1/ofertas/" + ofertaId)
                        .header("Authorization", "Bearer " + tokenDoador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Estante reformada","descricao":"Estante de madeira reformada",
                                "categoria":"movel","estadoItem":"usado","cidade":"Curitiba",
                                "precisaColeta":false,"disponivelAte":"2035-01-01"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprovado").value(false));
        assertFalse(aprovada(), "Edição deve desaprovar a oferta");

        // 2) Adicionar foto também exige nova moderação.
        reaprovar();
        MvcResult upload = mvc.perform(multipart("/api/v1/ofertas/" + ofertaId + "/imagens")
                        .file(foto("foto1.png"))
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aprovado").value(false))
                .andReturn();
        assertFalse(aprovada());
        long imagemId = objectMapper.readTree(upload.getResponse().getContentAsString())
                .get("imagens").get(0).get("id").asLong();

        // 3) Limite de 5 fotos no total (1 via API + 4 inseridas direto).
        Oferta o = ofertaRepository.findById(ofertaId).orElseThrow();
        for (int i = 2; i <= 5; i++) {
            OfertaImage img = new OfertaImage(o, "/uploads/ofertas/fake-" + i + ".png");
            img.setOrdem(i);
            imageRepository.save(img);
        }
        assertEquals(5, imageRepository.countByOfertaId(ofertaId));
        mvc.perform(multipart("/api/v1/ofertas/" + ofertaId + "/imagens")
                        .file(foto("foto6.png"))
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().isBadRequest());
        assertEquals(5, imageRepository.countByOfertaId(ofertaId), "6ª foto não pode ser gravada");

        // 4) Remover foto também devolve para moderação.
        reaprovar();
        mvc.perform(delete("/api/v1/ofertas/imagens/" + imagemId)
                        .header("Authorization", "Bearer " + tokenDoador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprovado").value(false));
        assertFalse(aprovada(), "Remoção de foto deve desaprovar a oferta");
        assertEquals(4, imageRepository.countByOfertaId(ofertaId));
        assertTrue(imageRepository.countByOfertaId(ofertaId) < 5);
    }
}
