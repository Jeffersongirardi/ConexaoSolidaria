package com.conexoessolidarias.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * MySQL gerenciado na Railway (profile prod).
 *
 * Fontes aceitas (nesta ordem):
 * 1. DATABASE_URL no formato mysql://usuario:senha@host:porta/banco
 * 2. Variáveis separadas MYSQLUSER/MYSQLPASSWORD/MYSQLHOST/MYSQLPORT/MYSQLDATABASE
 *    (injetadas automaticamente pela Railway quando o plugin MySQL está no projeto).
 */
@Configuration
@Profile("prod")
public class RailwayDataSourceConfig {

    @Bean
    public DataSource dataSource() {
        String raw = System.getenv("DATABASE_URL");
        if (raw != null && !raw.isBlank()) {
            return forUrl(raw);
        }
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv().getOrDefault("MYSQLPORT", "3306");
        String db = System.getenv("MYSQLDATABASE");
        if (user == null || pass == null || host == null || db == null) {
            throw new IllegalStateException(
                    "MySQL não configurado: defina DATABASE_URL (mysql://...) ou as variáveis MYSQL* da Railway");
        }
        return DataSourceBuilder.create()
                .url("jdbc:mysql://" + host + ":" + port + "/" + db
                        + "?useSSL=true&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8")
                .username(user)
                .password(pass)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    static DataSource forUrl(String raw) {
        ParsedUrl p = parse(raw);
        return DataSourceBuilder.create()
                .url(p.jdbcUrl())
                .username(p.username())
                .password(p.password())
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    record ParsedUrl(String jdbcUrl, String username, String password) {}

    /**
     * Converte mysql://user:pass@host:port/db?params em JDBC.
     * Lança IllegalArgumentException em formato inválido ou não-MySQL.
     */
    static ParsedUrl parse(String raw) {
        String s = raw.strip();
        if (!s.startsWith("mysql://")) {
            throw new IllegalArgumentException("DATABASE_URL deve começar com mysql://");
        }
        String noScheme = s.substring("mysql://".length());
        int at = noScheme.lastIndexOf('@');
        if (at < 0) throw new IllegalArgumentException("DATABASE_URL sem usuário/senha (@ ausente)");
        String creds = noScheme.substring(0, at);
        String hostPart = noScheme.substring(at + 1);
        int colon = creds.indexOf(':');
        if (colon < 0) throw new IllegalArgumentException("DATABASE_URL sem senha (: ausente)");
        String user = creds.substring(0, colon);
        String pass = creds.substring(colon + 1);
        if (user.isEmpty() || hostPart.isEmpty() || !hostPart.contains("/")) {
            throw new IllegalArgumentException("DATABASE_URL inválida");
        }
        String base = "jdbc:mysql://" + hostPart;
        String jdbc = base.contains("?")
                ? base + "&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8"
                : base + "?useSSL=true&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8";
        return new ParsedUrl(jdbc, user, pass);
    }
}
