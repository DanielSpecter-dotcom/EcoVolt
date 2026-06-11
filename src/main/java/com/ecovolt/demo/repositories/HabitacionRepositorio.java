package com.ecovolt.demo.repositories;

import com.ecovolt.demo.entities.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitacionRepositorio extends JpaRepository<Habitacion, Long> {

    Optional<Habitacion> findFirstByCasaIdOrderByIdAsc(Long casaId);

    Optional<Habitacion> findByIdAndCasaUsuarioId(Long id, Long usuarioId);

    Optional<Habitacion> findByIdAndCasaId(Long id, Long casaId);

    List<Habitacion> findByCasaUsuarioIdOrderByIdAsc(Long usuarioId);
}
