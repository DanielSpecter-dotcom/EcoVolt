package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.request.CrearRutinaDto;
import com.ecovolt.demo.dtos.request.ActualizarRutinaDto;
import com.ecovolt.demo.dtos.response.AccionDispositivoRutinaRespuestaDto;
import com.ecovolt.demo.dtos.response.RutinaRespuestaDto;
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
    private final Map<Long, RutinaRespuestaDto> routines = new ConcurrentHashMap<>();

    @Override
    public RutinaRespuestaDto create(CrearRutinaDto request) {
        RutinaRespuestaDto response = RutinaRespuestaDto.builder()
                .id(sequence.getAndIncrement())
                .homeId(request.getHomeId())
                .name(request.getNombre())
                .executionTime(request.getTiempoEjecucion())
                .daysOfWeek(new LinkedHashSet<>(request.getDiasSemana()))
                .actions(request.getAcciones().stream()
                        .map(action -> AccionDispositivoRutinaRespuestaDto.builder()
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
    public List<RutinaRespuestaDto> findAll() {
        return routines.values().stream()
                .sorted(java.util.Comparator.comparing(RutinaRespuestaDto::getId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RutinaRespuestaDto findById(Long routineId) {
        return findRoutine(routineId);
    }

    @Override
    public RutinaRespuestaDto update(Long routineId, ActualizarRutinaDto request) {
        RutinaRespuestaDto routine = findRoutine(routineId);

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
                    .map(action -> AccionDispositivoRutinaRespuestaDto.builder()
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
        for (RutinaRespuestaDto routine : routines.values()) {
            if (homeId.equals(routine.getHomeId())) {
                routine.setPausedByAwayMode(awayModeEnabled);
                updated++;
            }
        }
        return updated;
    }

    private RutinaRespuestaDto findRoutine(Long routineId) {
        RutinaRespuestaDto routine = routines.get(routineId);
        if (routine == null) {
            throw new ResourceNotFoundException("Rutina no encontrada");
        }
        return routine;
    }
}
