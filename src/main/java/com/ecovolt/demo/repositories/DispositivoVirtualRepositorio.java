package com.ecovolt.demo.repositories;

import com.ecovolt.demo.entities.DispositivoVirtual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DispositivoVirtualRepositorio extends JpaRepository<DispositivoVirtual, Long> {

    Optional<DispositivoVirtual> findByIdAndHabitacionCasaUsuarioId(Long id, Long usuarioId);

    List<DispositivoVirtual> findByHabitacionCasaUsuarioId(Long usuarioId);

    Optional<DispositivoVirtual> findByIdAndEliminadoFalse(Long id);

    List<DispositivoVirtual> findByEliminadoFalseOrderByIdAsc();
}
