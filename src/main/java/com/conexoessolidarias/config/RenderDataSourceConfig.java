package com.conexoessolidarias.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class RenderDataSourceConfig {

    @Bean
    public DataSource dataSource() {
        String raw = System.getenv("DATABASE_URL");
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("DATABASE_URL environment variable is required in prod profile");
        }
        String jdbcUrl = "jdbc:postgresql://" + raw.replaceFirst("^postgres://", "");
        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
