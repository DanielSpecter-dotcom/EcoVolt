package com.ecovolt.demo.repositories;

import com.ecovolt.demo.entities.Historico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoRepositorio extends JpaRepository<Historico, Long> {

    List<Historico> findByDispositivoId(Long dispositivoId);

    List<Historico> findByDispositivoHabitacionIdAndDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(
            Long habitacionId,
            Long usuarioId
    );

    List<Historico> findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(Long usuarioId);

    List<Historico> findByDispositivoIdAndDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(
            Long dispositivoId,
            Long usuarioId
    );
}
