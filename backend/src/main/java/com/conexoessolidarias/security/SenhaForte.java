package com.conexoessolidarias.security;

import java.util.Locale;
import java.util.Set;

/**
 * Política de senha do projeto: frases longas são melhores que regras de composição.
 * Mínimo de 10 caracteres, bloqueio de senhas comuns e de senha igual a e-mail/nome.
 */
public final class SenhaForte {

    public static final int MIN = 10;
    public static final int MAX = 128;

    private static final Set<String> COMUNS = Set.of(
            "1234567890", "12345678910", "senha123456", "senha12345", "senhaforte",
            "password123", "qwerty12345", "admin123456", "administrador", "conexoes123",
            "conexaosolidaria", "conexaosolidarias", "mudarsenha", "trocarsenha",
            "brasil12345", "curitiba123", "uninter2024", "uninter12345", "doacao12345");

    private SenhaForte() {
    }

    public static void validar(String senha, String email, String nome) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (senha.length() < MIN) {
            throw new IllegalArgumentException("A senha deve ter ao menos " + MIN + " caracteres");
        }
        if (senha.length() > MAX) {
            throw new IllegalArgumentException("A senha deve ter no máximo " + MAX + " caracteres");
        }
        String normalizada = senha.toLowerCase(Locale.ROOT);
        if (COMUNS.contains(normalizada)) {
            throw new IllegalArgumentException("Senha muito comum — escolha outra");
        }
        if (email != null && !email.isBlank()) {
            String mail = email.trim().toLowerCase(Locale.ROOT);
            String local = mail.contains("@") ? mail.substring(0, mail.indexOf('@')) : mail;
            if (normalizada.equals(mail) || (local.length() >= 3 && normalizada.equals(local))) {
                throw new IllegalArgumentException("A senha não pode ser igual ao e-mail");
            }
        }
        if (nome != null && !nome.isBlank()
                && normalizada.equals(nome.trim().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("A senha não pode ser igual ao seu nome");
        }
    }
}
