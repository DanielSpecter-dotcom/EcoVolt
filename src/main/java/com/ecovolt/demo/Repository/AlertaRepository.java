package com.ecovolt.demo.Repository;

import com.ecovolt.demo.Entities.AlertaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends JpaRepository<AlertaEntity, Long> {

    List<AlertaEntity> findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    Optional<AlertaEntity> findByIdAndDispositivoHabitacionCasaUsuarioId(Long id, Long usuarioId);
}
