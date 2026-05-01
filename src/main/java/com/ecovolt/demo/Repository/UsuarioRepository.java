package com.ecovolt.demo.Repository;

import com.ecovolt.demo.Entities.UsuarioEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    boolean existsByCorreo(String correo);

    boolean existsByDni(String dni);

    @EntityGraph(attributePaths = "roles")
    Optional<UsuarioEntity> findByCorreo(String correo);

    Optional<UsuarioEntity> findByVerificationToken(String verificationToken);
}
