package com.conexoessolidarias.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiDateTimeConfigTest {

    @Test
    void datasDaApiUsamOFusoConfiguradoESemAlterarDatasDeCalendario() throws Exception {
        var builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToDisable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        new ApiDateTimeConfig()
                .explicitDateTimeOffset("America/Sao_Paulo")
                .customize(builder);

        var mapper = builder.build();
        var date = LocalDateTime.of(2026, 11, 1, 19, 23);
        var serialized = mapper.writeValueAsString(date);

        assertEquals("\"2026-11-01T19:23-03:00\"", serialized);
        assertEquals(date.atZone(ZoneId.of("America/Sao_Paulo")).toInstant(),
                OffsetDateTime.parse("2026-11-01T19:23-03:00").toInstant());
        assertEquals("\"2026-11-01\"", mapper.writeValueAsString(LocalDate.of(2026, 11, 1)));
    }
}
