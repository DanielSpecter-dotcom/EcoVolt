package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.request.RoutineRequestDto;
import com.ecovolt.demo.dtos.request.RoutineUpdateRequestDto;
import com.ecovolt.demo.dtos.response.RoutineResponseDto;

import java.util.List;

public interface RoutineService {

    // Debe registrar la programacion y las acciones independientes por dispositivo.
    RoutineResponseDto create(RoutineRequestDto request);

    // Debe retornar las rutinas visibles para el dashboard.
    List<RoutineResponseDto> findAll();

    // Debe aplicar cambios parciales, incluida la pausa mediante enabled=false.
    RoutineResponseDto update(Long routineId, RoutineUpdateRequestDto request);

    // Debe eliminar definitivamente la rutina de la persistencia.
    void delete(Long routineId);

    // Debe pausar o liberar todas las rutinas automaticas asociadas a una casa.
    int applyAwayMode(Long homeId, boolean awayModeEnabled);
}
