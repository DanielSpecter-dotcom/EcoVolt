package com.ecovolt.demo.Repository;

import com.ecovolt.demo.Entities.HistoricoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoRepository extends JpaRepository<HistoricoEntity, Long> {

    List<HistoricoEntity> findByDispositivoHabitacionIdAndDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(
            Long habitacionId,
            Long usuarioId
    );

    List<HistoricoEntity> findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(Long usuarioId);

    List<HistoricoEntity> findByDispositivoIdAndDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(
            Long dispositivoId,
            Long usuarioId
    );
}
