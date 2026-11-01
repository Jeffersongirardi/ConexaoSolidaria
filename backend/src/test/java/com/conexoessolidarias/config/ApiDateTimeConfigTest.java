package com.conexoessolidarias.config;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ApiDateTimeConfigTest {
    @Test void datasDaApiIdentificamOInstanteSemAlterarDatasDeCalendario() throws Exception {
        var builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToDisable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        new ApiDateTimeConfig().explicitDateTimeOffset().customize(builder);
        var mapper = builder.build();
        var date = LocalDateTime.of(2026, 11, 1, 19, 23);
        var serialized = mapper.writeValueAsString(date);
        var parsed = OffsetDateTime.parse(serialized.substring(1, serialized.length() - 1));
        assertEquals(date.atZone(ZoneId.systemDefault()).toInstant(), parsed.toInstant());
        assertEquals("\"2026-11-01\"", mapper.writeValueAsString(LocalDate.of(2026, 11, 1)));
        assertEquals(16, OffsetDateTime.parse("2026-11-01T19:23Z")
                .atZoneSameInstant(ZoneId.of("America/Sao_Paulo")).getHour());
    }
}
