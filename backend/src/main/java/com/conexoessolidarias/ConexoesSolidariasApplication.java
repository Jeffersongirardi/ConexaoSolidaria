package com.conexoessolidarias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConexoesSolidariasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConexoesSolidariasApplication.class, args);
    }
}
