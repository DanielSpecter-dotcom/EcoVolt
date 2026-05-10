package com.ecovolt.demo.repositories;

import com.ecovolt.demo.entities.Casa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CasaRepositorio extends JpaRepository<Casa, Long> {

    Optional<Casa> findFirstByUsuarioIdOrderByIdAsc(Long usuarioId);
}
