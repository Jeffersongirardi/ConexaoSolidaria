package com.conexoessolidarias.config;

import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByTipo("admin").isEmpty()) {
            String email = System.getenv("ADMIN_EMAIL");
            String password = System.getenv("ADMIN_PASSWORD");
            if (email == null) email = "admin@conexoessolidarias.org";
            if (password == null) password = "admin123";
            User admin = new User(
                "Administrador",
                email,
                passwordEncoder.encode(password),
                "admin"
            );
            userRepository.save(admin);
        }
    }
}
