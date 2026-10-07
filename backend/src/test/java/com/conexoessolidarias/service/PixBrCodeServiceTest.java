package com.conexoessolidarias.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PixBrCodeServiceTest {

    private final PixBrCodeService pix = new PixBrCodeService();

    @Test
    void crc16_vetorPadrao() {
        // Check value oficial do CRC-16/CCITT-FALSE
        assertEquals("29B1", PixBrCodeService.crc16("123456789"));
    }

    @Test
    void gerar_payloadEstruturaValida() {
        String payload = pix.gerarCopiaECola("paginst@teste.org", new BigDecimal("50.00"),
                "Inst Pag", "Curitiba", null);
        assertTrue(payload.startsWith("000201"));
        assertTrue(payload.contains("br.gov.bcb.pix"));
        assertTrue(payload.contains("paginst@teste.org"));
        assertTrue(payload.contains("540550.00"));
        assertTrue(payload.contains("62070503***"));
        assertTrue(pix.validarCopiaECola(payload));
    }

    @Test
    void gerar_payloadAdulterado_rejeitado() {
        String payload = pix.gerarCopiaECola("paginst@teste.org", new BigDecimal("50.00"),
                "Inst Pag", "Curitiba", null);
        String adulterado = payload.replace("540550.00", "540599.99");
        assertFalse(pix.validarCopiaECola(adulterado));
    }

    @Test
    void normalizar_tipos() {
        assertEquals(PixBrCodeService.TipoChave.EMAIL,
                pix.normalizar("PagInst@Teste.Org").tipo());
        assertEquals("paginst@teste.org",
                pix.normalizar("PagInst@Teste.Org").valor());
        assertEquals(PixBrCodeService.TipoChave.CPF,
                pix.normalizar("529.982.247-25").tipo());
        assertEquals("52998224725", pix.normalizar("529.982.247-25").valor());
        assertEquals(PixBrCodeService.TipoChave.CNPJ,
                pix.normalizar("11.222.333/0001-81").tipo());
        assertEquals(PixBrCodeService.TipoChave.TELEFONE,
                pix.normalizar("+5561999999999").tipo());
        assertEquals("+5511999999999", pix.normalizar("(11) 99999-9999").valor());
        assertEquals(PixBrCodeService.TipoChave.ALEATORIA,
                pix.normalizar("123E4567-E89B-12D3-A456-426655440000").tipo());
    }

    @Test
    void normalizar_invalidas() {
        assertThrows(IllegalArgumentException.class, () -> pix.normalizar(null));
        assertThrows(IllegalArgumentException.class, () -> pix.normalizar("   "));
        assertThrows(IllegalArgumentException.class, () -> pix.normalizar("abc"));
        assertThrows(IllegalArgumentException.class, () -> pix.normalizar("123"));
        assertThrows(IllegalArgumentException.class, () -> pix.normalizar("111.111.111-11"));
        assertThrows(IllegalArgumentException.class, () -> pix.normalizar("nao-email"));
    }

    @Test
    void gerar_valorInvalido_rejeitado() {
        assertThrows(IllegalArgumentException.class, () -> pix.gerarCopiaECola(
                "a@b.co", BigDecimal.ZERO, "X", "Y", null));
        assertThrows(IllegalArgumentException.class, () -> pix.gerarCopiaECola(
                "a@b.co", new BigDecimal("-1"), "X", "Y", null));
    }

    @Test
    void gerar_txid() {
        String payload = pix.gerarCopiaECola("a@b.co", new BigDecimal("10.00"),
                "X", "Y", "PEDIDO123");
        assertTrue(payload.contains("PEDIDO123"));
        assertTrue(pix.validarCopiaECola(payload));
        assertThrows(IllegalArgumentException.class, () -> pix.gerarCopiaECola(
                "a@b.co", new BigDecimal("10.00"), "X", "Y", "com-traço"));
    }

    @Test
    void gerar_nomeCidadeNormalizados() {
        String payload = pix.gerarCopiaECola("a@b.co", new BigDecimal("10.00"),
                "Cantinho Feliz Ação", "São Paulo", null);
        assertTrue(payload.contains("CANTINHO FELIZ ACAO"));
        assertTrue(payload.contains("SAO PAULO"));
        assertTrue(pix.validarCopiaECola(payload));
    }
}
