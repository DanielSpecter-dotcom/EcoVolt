package com.ecovolt.demo.repositories;

import com.ecovolt.demo.entities.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RutinaRepositorio extends JpaRepository<Rutina, Long> {

    List<Rutina> findByCasaUsuarioIdOrderByIdAsc(Long usuarioId);

    Optional<Rutina> findByIdAndCasaUsuarioId(Long id, Long usuarioId);

    List<Rutina> findByCasaId(Long casaId);
}
