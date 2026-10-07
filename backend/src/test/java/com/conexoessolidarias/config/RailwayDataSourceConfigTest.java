package com.conexoessolidarias.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RailwayDataSourceConfigTest {

    @Test
    void parse_urlRailway() {
        var p = RailwayDataSourceConfig.parse(
                "mysql://root:senha123@mysql.railway.internal:3306/railway");
        assertEquals("jdbc:mysql://mysql.railway.internal:3306/railway"
                + "?useSSL=true&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8",
                p.jdbcUrl());
        assertEquals("root", p.username());
        assertEquals("senha123", p.password());
    }

    @Test
    void parse_preservaParametros() {
        var p = RailwayDataSourceConfig.parse(
                "mysql://u:p@host:3306/db?useSSL=false");
        assertTrue(p.jdbcUrl().startsWith("jdbc:mysql://host:3306/db"));
        assertTrue(p.jdbcUrl().contains("serverTimezone=America/Sao_Paulo"));
    }

    @Test
    void parse_formatosInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> RailwayDataSourceConfig.parse("postgres://u:p@h:5432/db"));
        assertThrows(IllegalArgumentException.class,
                () -> RailwayDataSourceConfig.parse("mysql://host/db"));
        assertThrows(IllegalArgumentException.class,
                () -> RailwayDataSourceConfig.parse("mysql://user@host/db"));
        assertThrows(IllegalArgumentException.class,
                () -> RailwayDataSourceConfig.parse("mysql://u:p@hostsemdb"));
    }
}
