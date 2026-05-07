package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.request.CrearRutinaDto;
import com.ecovolt.demo.dtos.request.ActualizarRutinaDto;
import com.ecovolt.demo.dtos.response.RutinaRespuestaDto;

import java.util.List;

public interface RoutineService {

    // Debe registrar la programacion y las acciones independientes por dispositivo.
    RutinaRespuestaDto create(CrearRutinaDto request);

    // Debe retornar las rutinas visibles para el dashboard.
    List<RutinaRespuestaDto> findAll();

    RutinaRespuestaDto findById(Long routineId);

    // Debe aplicar cambios parciales, incluida la pausa mediante enabled=false.
    RutinaRespuestaDto update(Long routineId, ActualizarRutinaDto request);

    // Debe eliminar definitivamente la rutina de la persistencia.
    void delete(Long routineId);

    // Debe pausar o liberar todas las rutinas automaticas asociadas a una casa.
    int applyAwayMode(Long homeId, boolean awayModeEnabled);
}
