package com.ecovolt.demo.repositories;

import com.ecovolt.demo.entities.Casa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CasaRepositorio extends JpaRepository<Casa, Long> {

    Optional<Casa> findFirstByUsuarioIdOrderByIdAsc(Long usuarioId);

    List<Casa> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    long countByUsuarioId(Long usuarioId);
}
