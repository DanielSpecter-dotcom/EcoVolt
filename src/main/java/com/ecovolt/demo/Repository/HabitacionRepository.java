package com.ecovolt.demo.Repository;

import com.ecovolt.demo.Entities.HabitacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HabitacionRepository extends JpaRepository<HabitacionEntity, Long> {

    Optional<HabitacionEntity> findFirstByCasaIdOrderByIdAsc(Long casaId);

    Optional<HabitacionEntity> findByIdAndCasaUsuarioId(Long id, Long usuarioId);
}
