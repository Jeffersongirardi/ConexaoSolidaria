package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.Oferta;
import com.conexoessolidarias.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OfertaDTOTest {

    private Oferta ofertaComContato() {
        User doador = new User("Maria", "maria@teste.org", "hash", "doador");
        doador.setId(7L);
        doador.setWhatsapp("41999998888");

        Oferta o = new Oferta();
        o.setDoador(doador);
        o.setTitulo("Sofá");
        o.setDescricao("Sofá em bom estado");
        o.setCategoria("movel");
        o.setCidade("Curitiba");
        o.setPrecisaColeta(true);
        o.setEnderecoColeta("Rua Sigilosa, 123");
        o.setDisponivelAte(LocalDate.now().plusDays(7));
        o.setStatus(Oferta.DISPONIVEL);
        o.setAprovado(true);
        return o;
    }

    @Test
    void fromPadraoOcultaContatoEEndereco() {
        OfertaDTO dto = OfertaDTO.from(ofertaComContato());

        assertNull(dto.doadorWhatsapp(), "WhatsApp não pode vazar no DTO padrão");
        assertNull(dto.enderecoColeta(), "Endereço de coleta não pode vazar no DTO padrão");
        assertEquals("Maria", dto.doadorNome(), "Nome do doador continua visível");
    }

    @Test
    void fromExplicitoExpoeContatoEEndereco() {
        OfertaDTO dto = OfertaDTO.from(ofertaComContato(), true);

        assertEquals("41999998888", dto.doadorWhatsapp());
        assertEquals("Rua Sigilosa, 123", dto.enderecoColeta());
    }
}
