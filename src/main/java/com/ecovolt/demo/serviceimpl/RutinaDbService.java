package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.CrearRutinaDto;
import com.ecovolt.demo.dtos.ActualizarRutinaDto;
import com.ecovolt.demo.dtos.AccionRutinaDTO;
import com.ecovolt.demo.dtos.RutinaDTO;
import com.ecovolt.demo.entities.Rutina;
import com.ecovolt.demo.entities.AccionRutina;
import com.ecovolt.demo.entities.Casa;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.exceptions.BadRequestException;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.RutinaRepositorio;
import com.ecovolt.demo.repositories.CasaRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import com.ecovolt.demo.services.RoutineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
@Transactional
public class RutinaDbService implements RoutineService {

    @Autowired
    private RutinaRepositorio rutinaRepositorio;

    @Autowired
    private CasaRepositorio casaRepositorio;

    @Autowired
    private DispositivoVirtualRepositorio dispositivoVirtualRepositorio;

    @Override
    public RutinaDTO create(CrearRutinaDto request, Long usuarioId) {
        Casa casa = casaRepositorio.findById(request.getHomeId())
                .orElseThrow(() -> new ResourceNotFoundException("La casa indicada no existe"));

        if (!casa.getUsuario().getId().equals(usuarioId)) {
            throw new BadRequestException("La casa indicada no pertenece al usuario autenticado");
        }

        Rutina rutina = Rutina.builder()
                .casa(casa)
                .nombre(request.getNombre())
                .tiempoEjecucion(request.getTiempoEjecucion())
                .diasSemana(new LinkedHashSet<>(request.getDiasSemana()))
                .activo(true)
                .pausadoAusente(false)
                .build();

        List<AccionRutina> acciones = new ArrayList<>();
        if (request.getAcciones() != null) {
            for (var actionDto : request.getAcciones()) {
                DispositivoVirtual dispositivo = dispositivoVirtualRepositorio.findById(actionDto.getDeviceId())
                        .orElseThrow(() -> new ResourceNotFoundException("El dispositivo con ID " + actionDto.getDeviceId() + " no existe"));

                acciones.add(AccionRutina.builder()
                        .rutina(rutina)
                        .dispositivo(dispositivo)
                        .encender(actionDto.getEncendido())
                        .build());
            }
        }
        rutina.setAcciones(acciones);

        rutina = rutinaRepositorio.save(rutina);
        return mapToDto(rutina);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutinaDTO> findAll() {
        return rutinaRepositorio.findAll().stream()
                .map(this::mapToDto)
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutinaDTO> findAll(Long usuarioId) {
        List<Long> userHomeIds = casaRepositorio.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
                .map(Casa::getId)
                .toList();

        return rutinaRepositorio.findAll().stream()
                .filter(r -> userHomeIds.contains(r.getCasa().getId()))
                .map(this::mapToDto)
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RutinaDTO findById(Long routineId, Long usuarioId) {
        Rutina rutina = findRoutine(routineId, usuarioId);
        return mapToDto(rutina);
    }

    @Override
    public RutinaDTO update(Long routineId, ActualizarRutinaDto request, Long usuarioId) {
        Rutina rutina = findRoutine(routineId, usuarioId);

        if (request.getHomeId() != null) {
            Casa casa = casaRepositorio.findById(request.getHomeId())
                    .orElseThrow(() -> new ResourceNotFoundException("La casa indicada no existe"));

            if (!casa.getUsuario().getId().equals(usuarioId)) {
                throw new BadRequestException("La casa indicada no pertenece al usuario autenticado");
            }
            rutina.setCasa(casa);
        }

        if (request.getName() != null) {
            rutina.setNombre(request.getName());
        }

        if (request.getTiempoEjecucion() != null) {
            rutina.setTiempoEjecucion(request.getTiempoEjecucion());
        }

        if (request.getDiasSemana() != null) {
            rutina.setDiasSemana(new LinkedHashSet<>(request.getDiasSemana()));
        }

        if (request.getAcciones() != null) {
            rutina.getAcciones().clear();
            for (var actionDto : request.getAcciones()) {
                DispositivoVirtual dispositivo = dispositivoVirtualRepositorio.findById(actionDto.getDeviceId())
                        .orElseThrow(() -> new ResourceNotFoundException("El dispositivo con ID " + actionDto.getDeviceId() + " no existe"));

                rutina.getAcciones().add(AccionRutina.builder()
                        .rutina(rutina)
                        .dispositivo(dispositivo)
                        .encender(actionDto.getEncendido())
                        .build());
            }
        }

        if (request.getHabilitar() != null) {
            rutina.setActivo(request.getHabilitar());
        }

        rutina = rutinaRepositorio.save(rutina);
        return mapToDto(rutina);
    }

    @Override
    public void delete(Long routineId, Long usuarioId) {
        Rutina rutina = findRoutine(routineId, usuarioId);
        rutinaRepositorio.delete(rutina);
    }

    @Override
    public int applyAwayMode(Long homeId, boolean awayModeEnabled) {
        List<Rutina> rutinas = rutinaRepositorio.findAll().stream()
                .filter(r -> r.getCasa().getId().equals(homeId))
                .toList();

        for (Rutina rutina : rutinas) {
            rutina.setPausadoAusente(awayModeEnabled);
            rutinaRepositorio.save(rutina);
        }
        return rutinas.size();
    }

    private Rutina findRoutine(Long routineId, Long usuarioId) {
        Rutina rutina = rutinaRepositorio.findById(routineId)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        if (!rutina.getCasa().getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Rutina no encontrada");
        }
        return rutina;
    }

    private RutinaDTO mapToDto(Rutina r) {
        Set<AccionRutinaDTO> actions = r.getAcciones().stream()
                .map(action -> AccionRutinaDTO.builder()
                        .deviceId(action.getDispositivo().getId())
                        .turnOn(action.isEncender())
                        .build())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return RutinaDTO.builder()
                .id(r.getId())
                .homeId(r.getCasa().getId())
                .name(r.getNombre())
                .executionTime(r.getTiempoEjecucion())
                .daysOfWeek(new LinkedHashSet<>(r.getDiasSemana()))
                .actions(actions)
                .enabled(r.isActivo())
                .pausedByAwayMode(r.isPausadoAusente())
                .build();
    }
}
