package com.conexoessolidarias.repository;

import com.conexoessolidarias.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndTipo(String email, String tipo);
    Optional<User> findByTipo(String tipo);
    boolean existsByEmail(String email);
}
