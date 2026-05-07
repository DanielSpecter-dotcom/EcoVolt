package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.CrearRutinaDto;
import com.ecovolt.demo.dtos.ActualizarRutinaDto;
import com.ecovolt.demo.dtos.AccionRutinaDTO;
import com.ecovolt.demo.dtos.RutinaDTO;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.services.RoutineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class RutinaMemoriaService implements RoutineService {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, RutinaDTO> routines = new ConcurrentHashMap<>();

    @Override
    public RutinaDTO create(CrearRutinaDto request) {
        RutinaDTO response = RutinaDTO.builder()
                .id(sequence.getAndIncrement())
                .homeId(request.getHomeId())
                .name(request.getNombre())
                .executionTime(request.getTiempoEjecucion())
                .daysOfWeek(new LinkedHashSet<>(request.getDiasSemana()))
                .actions(request.getAcciones().stream()
                        .map(action -> AccionRutinaDTO.builder()
                                .deviceId(action.getDeviceId())
                                .turnOn(action.getEncendido())
                                .build())
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)))
                .enabled(true)
                .pausedByAwayMode(false)
                .build();

        routines.put(response.getId(), response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutinaDTO> findAll() {
        return routines.values().stream()
                .sorted(java.util.Comparator.comparing(RutinaDTO::getId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RutinaDTO findById(Long routineId) {
        return findRoutine(routineId);
    }

    @Override
    public RutinaDTO update(Long routineId, ActualizarRutinaDto request) {
        RutinaDTO routine = findRoutine(routineId);

        /*
         * La implementacion productiva debe persistir cambios parciales:
         * configuracion de horario/dias/acciones y el flag enabled para pausar
         * la programacion sin eliminar el registro.
         */
        if (request.getHomeId() != null) {
            routine.setHomeId(request.getHomeId());
        }
        if (request.getName() != null) {
            routine.setName(request.getName());
        }
        if (request.getTiempoEjecucion() != null) {
            routine.setExecutionTime(request.getTiempoEjecucion());
        }
        if (request.getDiasSemana() != null) {
            routine.setDaysOfWeek(new LinkedHashSet<>(request.getDiasSemana()));
        }
        if (request.getAcciones() != null) {
            routine.setActions(request.getAcciones().stream()
                    .map(action -> AccionRutinaDTO.builder()
                            .deviceId(action.getDeviceId())
                            .turnOn(action.getEncendido())
                            .build())
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        }
        if (request.getHabilitar() != null) {
            routine.setEnabled(request.getHabilitar());
        }

        routines.put(routineId, routine);
        return routine;
    }

    @Override
    public void delete(Long routineId) {
        if (routines.remove(routineId) == null) {
            throw new ResourceNotFoundException("Rutina no encontrada");
        }
    }

    @Override
    public int applyAwayMode(Long homeId, boolean awayModeEnabled) {
        /*
         * La implementacion productiva debe actualizar todas las rutinas
         * automaticas de la casa. Al activar modo ausente se pausan; al
         * desactivarlo se liberan para que el scheduler vuelva a evaluarlas.
         */
        int updated = 0;
        for (RutinaDTO routine : routines.values()) {
            if (homeId.equals(routine.getHomeId())) {
                routine.setPausedByAwayMode(awayModeEnabled);
                updated++;
            }
        }
        return updated;
    }

    private RutinaDTO findRoutine(Long routineId) {
        RutinaDTO routine = routines.get(routineId);
        if (routine == null) {
            throw new ResourceNotFoundException("Rutina no encontrada");
        }
        return routine;
    }
}
