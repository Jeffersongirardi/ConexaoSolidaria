package com.conexoessolidarias.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Geração e validação de BR Code PIX (padrão Banco Central / EMVCo).
 *
 * O dinheiro vai DIRETO para a conta da instituição — a plataforma só gera
 * o QR/copia-e-cola a partir da chave PIX cadastrada pela instituição.
 * Sem gateway, sem custódia, sem repasse (ver Termos de Uso).
 *
 * Layout: 00="01" + 26(len + 00="br.gov.bcb.pix" + 01=chave)
 * + 52="0000" + 53="986" + [54=valor] + 58="BR" + 59=nome + 60=cidade
 * + 62(len + 05=txid) + 63="04"+CRC16-CCITT(0xFFFF) do payload + "6304".
 */
@Service
public class PixBrCodeService {

    public enum TipoChave { CPF, CNPJ, EMAIL, TELEFONE, ALEATORIA }

    public record ChavePix(TipoChave tipo, String valor) {}

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern UUID = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern TXID = Pattern.compile("^[a-zA-Z0-9]{1,25}$");

    /**
     * Detecta o tipo e normaliza a chave. Lança IllegalArgumentException se inválida.
     */
    public ChavePix normalizar(String chave) {
        if (chave == null) throw new IllegalArgumentException("Chave PIX inválida");
        String v = chave.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("Chave PIX inválida");

        if (EMAIL.matcher(v).matches()) {
            return new ChavePix(TipoChave.EMAIL, v.toLowerCase(Locale.ROOT));
        }
        if (UUID.matcher(v).matches()) {
            return new ChavePix(TipoChave.ALEATORIA, v.toLowerCase(Locale.ROOT));
        }
        String digitos = v.replaceAll("\\D", "");
        boolean soFormatoNumerico = v.matches("[0-9.\\-\\s/()+]+");
        if ((digitos.length() == 11 || digitos.length() == 14) && digitos.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("Chave PIX inválida");
        }
        if (digitos.length() == 11 && soFormatoNumerico) {
            if (cpfValido(digitos)) return new ChavePix(TipoChave.CPF, digitos);
            // 11 dígitos que não passam no CPF = telefone sem código do país (DDD + 9 dígitos)
            return new ChavePix(TipoChave.TELEFONE, "+55" + digitos);
        }
        if (digitos.length() == 14 && soFormatoNumerico) {
            if (!cnpjValido(digitos)) throw new IllegalArgumentException("CNPJ inválido como chave PIX");
            return new ChavePix(TipoChave.CNPJ, digitos);
        }
        // Telefone: +55 DDD número (12 ou 13 dígitos com código do país)
        String fone = v.startsWith("+") ? "+" + digitos : digitos;
        String soDig = fone.startsWith("+") ? fone.substring(1) : fone;
        if (soDig.startsWith("55") && (soDig.length() == 12 || soDig.length() == 13)) {
            return new ChavePix(TipoChave.TELEFONE, "+" + soDig);
        }
        throw new IllegalArgumentException("Chave PIX inválida: use CPF, CNPJ, e-mail, telefone (+55...) ou chave aleatória");
    }

    /**
     * Gera o código copia-e-cola. txid null/blank vira "***" (estático).
     */
    public String gerarCopiaECola(String chave, BigDecimal valor, String nomeRecebedor,
                                  String cidade, String txid) {
        ChavePix cp = normalizar(chave);
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("Valor do PIX deve ser positivo");
        }
        String nome = normalizarTexto(nomeRecebedor, 25, "RECEBEDOR");
        String cid = normalizarTexto(cidade, 15, "BRASIL");
        String tid = (txid == null || txid.isBlank()) ? "***" : txid.strip();
        if (!"***".equals(tid) && !TXID.matcher(tid).matches()) {
            throw new IllegalArgumentException("txid deve ter 1-25 caracteres alfanuméricos");
        }
        String gui = campo("00", "br.gov.bcb.pix") + campo("01", cp.valor());
        StringBuilder sb = new StringBuilder();
        sb.append(campo("00", "01"));
        sb.append(campo("26", gui));
        sb.append(campo("52", "0000"));
        sb.append(campo("53", "986"));
        sb.append(campo("54", String.format(Locale.US, "%.2f", valor)));
        sb.append(campo("58", "BR"));
        sb.append(campo("59", nome));
        sb.append(campo("60", cid));
        sb.append(campo("62", campo("05", tid)));
        sb.append("6304");
        sb.append(crc16(sb.toString()));
        return sb.toString();
    }

    /** Revalida um payload pronto (estrutura + CRC). */
    public boolean validarCopiaECola(String payload) {
        if (payload == null || payload.length() < 8 || !payload.startsWith("000201")) return false;
        if (!payload.substring(payload.length() - 8, payload.length() - 4).equals("6304")) return false;
        String esperado = crc16(payload.substring(0, payload.length() - 4));
        return payload.substring(payload.length() - 4).equalsIgnoreCase(esperado);
    }

    /** CRC16-CCITT-FALSE (poly 0x1021, init 0xFFFF). Ex.: "123456789" -> "29B1". */
    public static String crc16(String payload) {
        int crc = 0xFFFF;
        for (byte b : payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? ((crc << 1) ^ 0x1021) : (crc << 1);
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }

    private static String campo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    static String normalizarTexto(String texto, int max, String padrao) {
        String t = texto == null ? "" : Normalizer.normalize(texto.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9 .'-]", " ").strip();
        if (t.isBlank()) t = padrao;
        return t.length() > max ? t.substring(0, max) : t;
    }

    private static boolean cpfValido(String d) {
        if (d.chars().distinct().count() == 1) return false;
        int s1 = 0, s2 = 0;
        for (int i = 0; i < 9; i++) {
            int n = d.charAt(i) - '0';
            s1 += n * (10 - i);
            s2 += n * (11 - i);
        }
        int dv1 = s1 % 11 < 2 ? 0 : 11 - s1 % 11;
        s2 += dv1 * 2;
        int dv2 = s2 % 11 < 2 ? 0 : 11 - s2 % 11;
        return d.charAt(9) - '0' == dv1 && d.charAt(10) - '0' == dv2;
    }

    private static boolean cnpjValido(String d) {
        if (d.chars().distinct().count() == 1) return false;
        int[] p1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] p2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int s = 0;
        for (int i = 0; i < 12; i++) s += (d.charAt(i) - '0') * p1[i];
        int dv1 = s % 11 < 2 ? 0 : 11 - s % 11;
        s = 0;
        for (int i = 0; i < 12; i++) s += (d.charAt(i) - '0') * p2[i];
        s += dv1 * p2[12];
        int dv2 = s % 11 < 2 ? 0 : 11 - s % 11;
        return d.charAt(12) - '0' == dv1 && d.charAt(13) - '0' == dv2;
    }
}
