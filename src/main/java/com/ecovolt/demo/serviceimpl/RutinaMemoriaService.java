package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.AccionDispositivoRutinaDto;
import com.ecovolt.demo.dtos.ActualizarRutinaDto;
import com.ecovolt.demo.dtos.AccionRutinaDTO;
import com.ecovolt.demo.dtos.CrearRutinaDto;
import com.ecovolt.demo.dtos.RutinaDTO;
import com.ecovolt.demo.entities.AccionRutina;
import com.ecovolt.demo.entities.Casa;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.entities.Rutina;
import com.ecovolt.demo.exceptions.BadRequestException;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.CasaRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import com.ecovolt.demo.repositories.RutinaRepositorio;
import com.ecovolt.demo.services.RoutineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RutinaMemoriaService implements RoutineService {

    @Autowired
    private CasaRepositorio casaRepositorio;

    @Autowired
    private RutinaRepositorio rutinaRepositorio;

    @Autowired
    private DispositivoVirtualRepositorio dispositivoVirtualRepositorio;

    @Override
    public RutinaDTO create(CrearRutinaDto request, Long usuarioId) {
        Casa casa = findHomeOfUser(request.getHomeId(), usuarioId);

        Rutina rutina = Rutina.builder()
                .nombre(request.getNombre())
                .casa(casa)
                .executionTime(request.getTiempoEjecucion())
                .daysOfWeek(new LinkedHashSet<>(request.getDiasSemana()))
                .enabled(true)
                .pausedByAwayMode(false)
                .build();

        rutina.setAcciones(buildAcciones(rutina, request.getAcciones()));
        rutina = rutinaRepositorio.save(rutina);

        return toDTO(rutina);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutinaDTO> findAll() {
        return rutinaRepositorio.findAll()
                .stream()
                .sorted(java.util.Comparator.comparing(Rutina::getId))
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutinaDTO> findAll(Long usuarioId) {
        return rutinaRepositorio.findByCasaUsuarioIdOrderByIdAsc(usuarioId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RutinaDTO findById(Long routineId, Long usuarioId) {
        return toDTO(findRoutine(routineId, usuarioId));
    }

    @Override
    public RutinaDTO update(Long routineId, ActualizarRutinaDto request, Long usuarioId) {
        Rutina rutina = findRoutine(routineId, usuarioId);

        if (request.getHomeId() != null) {
            Casa casa = findHomeOfUser(request.getHomeId(), usuarioId);
            rutina.setCasa(casa);
        }
        if (request.getName() != null) {
            rutina.setNombre(request.getName());
        }
        if (request.getTiempoEjecucion() != null) {
            rutina.setExecutionTime(request.getTiempoEjecucion());
        }
        if (request.getDiasSemana() != null) {
            rutina.setDaysOfWeek(new LinkedHashSet<>(request.getDiasSemana()));
        }
        if (request.getAcciones() != null) {
            rutina.getAcciones().clear();
            rutina.getAcciones().addAll(buildAcciones(rutina, request.getAcciones()));
        }
        if (request.getHabilitar() != null) {
            rutina.setEnabled(request.getHabilitar());
        }

        rutina = rutinaRepositorio.save(rutina);
        return toDTO(rutina);
    }

    @Override
    public void delete(Long routineId, Long usuarioId) {
        Rutina rutina = findRoutine(routineId, usuarioId);
        rutinaRepositorio.delete(rutina);
    }

    @Override
    public int applyAwayMode(Long homeId, boolean awayModeEnabled) {
        List<Rutina> rutinas = rutinaRepositorio.findByCasaId(homeId);
        rutinas.forEach(rutina -> rutina.setPausedByAwayMode(awayModeEnabled));
        rutinaRepositorio.saveAll(rutinas);
        return rutinas.size();
    }

    private Rutina findRoutine(Long routineId, Long usuarioId) {
        return rutinaRepositorio.findByIdAndCasaUsuarioId(routineId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));
    }

    private Casa findHomeOfUser(Long homeId, Long usuarioId) {
        Casa casa = casaRepositorio.findById(homeId)
                .orElseThrow(() -> new BadRequestException("La casa indicada no pertenece al usuario autenticado"));
        if (!casa.getUsuario().getId().equals(usuarioId)) {
            throw new BadRequestException("La casa indicada no pertenece al usuario autenticado");
        }
        return casa;
    }

    private List<AccionRutina> buildAcciones(Rutina rutina, Set<AccionDispositivoRutinaDto> acciones) {
        return acciones.stream()
                .map(accion -> {
                    DispositivoVirtual dispositivo = dispositivoVirtualRepositorio.findById(accion.getDeviceId())
                            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));
                    return AccionRutina.builder()
                            .rutina(rutina)
                            .dispositivo(dispositivo)
                            .turnOn(accion.getEncendido())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private RutinaDTO toDTO(Rutina rutina) {
        Set<AccionRutinaDTO> actions = rutina.getAcciones() == null
                ? new LinkedHashSet<>()
                : rutina.getAcciones().stream()
                        .map(accion -> AccionRutinaDTO.builder()
                                .deviceId(accion.getDispositivo().getId())
                                .turnOn(accion.getTurnOn())
                                .build())
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        return RutinaDTO.builder()
                .id(rutina.getId())
                .homeId(rutina.getCasa().getId())
                .name(rutina.getNombre())
                .executionTime(rutina.getExecutionTime())
                .daysOfWeek(new LinkedHashSet<>(rutina.getDaysOfWeek()))
                .actions(actions)
                .enabled(rutina.isEnabled())
                .pausedByAwayMode(rutina.isPausedByAwayMode())
                .build();
    }
}
