package com.conexoessolidarias.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Configuration
public class ApiDateTimeConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer explicitDateTimeOffset(
            @Value("${app.timezone:America/Sao_Paulo}") String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        return builder -> builder.serializerByType(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString(value.atZone(zone).toOffsetDateTime().toString());
            }
        });
    }
}
