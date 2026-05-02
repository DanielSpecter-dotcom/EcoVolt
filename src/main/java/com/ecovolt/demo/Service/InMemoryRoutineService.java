package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.RoutineRequestDto;
import com.ecovolt.demo.Dto.Request.RoutineUpdateRequestDto;
import com.ecovolt.demo.Dto.Response.RoutineDeviceActionResponseDto;
import com.ecovolt.demo.Dto.Response.RoutineResponseDto;
import com.ecovolt.demo.Exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryRoutineService implements RoutineService {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, RoutineResponseDto> routines = new ConcurrentHashMap<>();

    @Override
    public RoutineResponseDto create(RoutineRequestDto request) {
        RoutineResponseDto response = RoutineResponseDto.builder()
                .id(sequence.getAndIncrement())
                .homeId(request.getHomeId())
                .name(request.getName())
                .executionTime(request.getExecutionTime())
                .daysOfWeek(new LinkedHashSet<>(request.getDaysOfWeek()))
                .actions(request.getActions().stream()
                        .map(action -> RoutineDeviceActionResponseDto.builder()
                                .deviceId(action.getDeviceId())
                                .turnOn(action.getTurnOn())
                                .build())
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)))
                .enabled(true)
                .pausedByAwayMode(false)
                .build();

        routines.put(response.getId(), response);
        return response;
    }

    @Override
    public List<RoutineResponseDto> findAll() {
        return routines.values().stream()
                .sorted(java.util.Comparator.comparing(RoutineResponseDto::getId))
                .toList();
    }

    @Override
    public RoutineResponseDto update(Long routineId, RoutineUpdateRequestDto request) {
        RoutineResponseDto routine = findRoutine(routineId);

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
        if (request.getExecutionTime() != null) {
            routine.setExecutionTime(request.getExecutionTime());
        }
        if (request.getDaysOfWeek() != null) {
            routine.setDaysOfWeek(new LinkedHashSet<>(request.getDaysOfWeek()));
        }
        if (request.getActions() != null) {
            routine.setActions(request.getActions().stream()
                    .map(action -> RoutineDeviceActionResponseDto.builder()
                            .deviceId(action.getDeviceId())
                            .turnOn(action.getTurnOn())
                            .build())
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        }
        if (request.getEnabled() != null) {
            routine.setEnabled(request.getEnabled());
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
        for (RoutineResponseDto routine : routines.values()) {
            if (homeId.equals(routine.getHomeId())) {
                routine.setPausedByAwayMode(awayModeEnabled);
                updated++;
            }
        }
        return updated;
    }

    private RoutineResponseDto findRoutine(Long routineId) {
        RoutineResponseDto routine = routines.get(routineId);
        if (routine == null) {
            throw new ResourceNotFoundException("Rutina no encontrada");
        }
        return routine;
    }
}
