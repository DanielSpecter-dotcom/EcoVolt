package com.ecovolt.demo.repositories;

import com.ecovolt.demo.entities.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    boolean existsByCorreo(String correo);

    boolean existsByDni(String dni);

    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByVerificationToken(String verificationToken);
}
