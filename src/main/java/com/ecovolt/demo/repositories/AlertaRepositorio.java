package com.ecovolt.demo.repositories;

import com.ecovolt.demo.entities.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaRepositorio extends JpaRepository<Alerta, Long> {

    List<Alerta> findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    Optional<Alerta> findByIdAndDispositivoHabitacionCasaUsuarioId(Long id, Long usuarioId);
}
