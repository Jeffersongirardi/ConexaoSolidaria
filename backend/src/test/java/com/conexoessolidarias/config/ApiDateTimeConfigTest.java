package com.conexoessolidarias.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiDateTimeConfigTest {

    @Test
    void datasDaApiUsamOFusoDaJvmESemAlterarDatasDeCalendario() throws Exception {
        var original = TimeZone.getDefault();
        try {
            var date = LocalDateTime.of(2026, 11, 1, 19, 23);
            for (var zone : new String[] {"UTC", "America/Sao_Paulo"}) {
                TimeZone.setDefault(TimeZone.getTimeZone(zone));
                var builder = new Jackson2ObjectMapperBuilder();
                builder.featuresToDisable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                new ApiDateTimeConfig().explicitDateTimeOffset().customize(builder);
                var mapper = builder.build();
                var serialized = mapper.writeValueAsString(date);
                var parsed = OffsetDateTime.parse(serialized.substring(1, serialized.length() - 1));
                assertEquals(date.atZone(ZoneId.of(zone)).toInstant(), parsed.toInstant());
                assertEquals("\"2026-11-01\"", mapper.writeValueAsString(LocalDate.of(2026, 11, 1)));
            }
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
