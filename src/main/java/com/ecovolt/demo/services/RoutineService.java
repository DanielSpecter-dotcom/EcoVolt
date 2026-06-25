package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.CrearRutinaDto;
import com.ecovolt.demo.dtos.ActualizarRutinaDto;
import com.ecovolt.demo.dtos.RutinaDTO;

import java.util.List;

public interface RoutineService {

    // Debe registrar la programacion y las acciones independientes por dispositivo.
    RutinaDTO create(CrearRutinaDto request, Long usuarioId);

    // Debe retornar las rutinas visibles para el dashboard.
    List<RutinaDTO> findAll();

    List<RutinaDTO> findAll(Long usuarioId);

    RutinaDTO findById(Long routineId, Long usuarioId);

    // Debe aplicar cambios parciales, incluida la pausa mediante enabled=false.
    RutinaDTO update(Long routineId, ActualizarRutinaDto request, Long usuarioId);

    // Debe eliminar definitivamente la rutina de la persistencia.
    void delete(Long routineId, Long usuarioId);

    // Debe pausar o liberar todas las rutinas automaticas asociadas a una casa.
    int applyAwayMode(Long homeId, boolean awayModeEnabled);
}
