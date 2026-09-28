package com.conexoessolidarias.model;

import java.util.Set;

/**
 * Categorias oficiais de campanha/doação. Valores técnicos estáveis
 * (sem migração de dados); rótulos plurais ficam no frontend.
 */
public final class Categoria {

    public static final Set<String> VALIDAS = Set.of(
            "alimento", "roupa", "calcado", "higiene", "fralda",
            "material_escolar", "brinquedo", "movel", "racao_animal",
            "emergencia", "outro");

    private Categoria() {}

    public static String normalizar(String categoria) {
        if (categoria == null) return "outro";
        String v = categoria.trim().toLowerCase();
        return VALIDAS.contains(v) ? v : "outro";
    }
}
