package com.conexoessolidarias.config;

import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      Environment env) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByTipo("admin").isEmpty()) {
            String email = System.getenv("ADMIN_EMAIL");
            String password = System.getenv("ADMIN_PASSWORD");
            if (email == null) email = "admin@conexoessolidarias.org";
            if (password == null) password = "admin123";

            boolean prod = Arrays.asList(env.getActiveProfiles()).contains("prod");
            if (prod) {
                validarAdminProducao(email, password);
            }

            User admin = new User(
                "Administrador",
                email,
                passwordEncoder.encode(password),
                "admin"
            );
            userRepository.save(admin);
        }
    }

    static void validarAdminProducao(String email, String password) {
        if (password == null || password.length() < 12
                || "admin123".equals(password)
                || password.equalsIgnoreCase(email)) {
            throw new IllegalStateException(
                    "Em produção, ADMIN_PASSWORD é obrigatória e deve ter ao menos 12 caracteres "
                            + "(diferente do e-mail e da senha padrão).");
        }
    }
}
