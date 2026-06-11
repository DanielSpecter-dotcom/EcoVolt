package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.components.SimuladorEnergiaUtils;
import com.ecovolt.demo.dtos.CrearDispositivoDto;
import com.ecovolt.demo.dtos.ModoDispositivoDto;
import com.ecovolt.demo.dtos.AsignarHabitacionDispositivoDto;
import com.ecovolt.demo.dtos.EstadoActualDispositivoDto;
import com.ecovolt.demo.dtos.ActualizarDispositivoDto;
import com.ecovolt.demo.dtos.DispositivoDTO;
import com.ecovolt.demo.entities.Habitacion;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.HabitacionRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DispositivoService {

    private final HabitacionRepositorio habitacionRepositorio;
    private final DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    private final HistoricoRepositorio historicoRepositorio;
    private final ModelMapper modelMapper;

    public DispositivoService(HabitacionRepositorio habitacionRepositorio,
                              DispositivoVirtualRepositorio dispositivoVirtualRepositorio,
                              HistoricoRepositorio historicoRepositorio,
                              ModelMapper modelMapper) {
        this.habitacionRepositorio = habitacionRepositorio;
        this.dispositivoVirtualRepositorio = dispositivoVirtualRepositorio;
        this.historicoRepositorio = historicoRepositorio;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public DispositivoDTO create(CrearDispositivoDto request) {
        Habitacion habitacion = habitacionRepositorio.findById(request.getHabitacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));

        DispositivoVirtual dispositivo = modelMapper.map(request, DispositivoVirtual.class);
        dispositivo.setNombre(request.getNombre().trim());
        dispositivo.setTipo(request.getTipo().trim());
        dispositivo.setPotenciaWatts(SimuladorEnergiaUtils.obtenerPotenciaBaseWatts(request.getTipo()));
        dispositivo.setActivo(request.getActivo());
        dispositivo.setAutomatico(request.getAutomatico());
        dispositivo.setEliminado(false);
        dispositivo.setLimiteKwh(request.getLimiteKwh());
        dispositivo.setHabitacion(habitacion);
        dispositivo = dispositivoVirtualRepositorio.save(dispositivo);

        historicoRepositorio.saveAll(buildSimulatedConsumption(dispositivo));

        DispositivoDTO dispositivoDTO = modelMapper.map(dispositivo, DispositivoDTO.class);
        dispositivoDTO.setStatus(dispositivo.isActivo() ? "ON" : "OFF");
        dispositivoDTO.setMode(dispositivo.isAutomatico() ? "AUTOMATIC" : "MANUAL");
        dispositivoDTO.setHabitacionId(dispositivo.getHabitacion().getId());
        dispositivoDTO.setHabitacionNombre(dispositivo.getHabitacion().getNombre());
        return dispositivoDTO;
    }

    @Transactional
    public DispositivoDTO assignRoom(Long id, AsignarHabitacionDispositivoDto request) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        Habitacion habitacion = findRoomInSameHome(dispositivo, request.getRoomId());

        dispositivo.setHabitacion(habitacion);
        dispositivo = dispositivoVirtualRepositorio.save(dispositivo);
        DispositivoDTO dispositivoDTO = modelMapper.map(dispositivo, DispositivoDTO.class);
        dispositivoDTO.setStatus(dispositivo.isActivo() ? "ON" : "OFF");
        dispositivoDTO.setMode(dispositivo.isAutomatico() ? "AUTOMATIC" : "MANUAL");
        dispositivoDTO.setHabitacionId(dispositivo.getHabitacion().getId());
        dispositivoDTO.setHabitacionNombre(dispositivo.getHabitacion().getNombre());
        return dispositivoDTO;
    }

    @Transactional
    public DispositivoDTO update(Long id, ActualizarDispositivoDto request) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        Habitacion habitacion = request.getRoomId() == null
                ? dispositivo.getHabitacion()
                : findRoomInSameHome(dispositivo, request.getRoomId());
        double newPower = SimuladorEnergiaUtils.obtenerPotenciaBaseWatts(request.getTipo());
        boolean powerChanged = dispositivo.getPotenciaWatts() == null
                || Double.compare(dispositivo.getPotenciaWatts(), newPower) != 0;

        dispositivo.setNombre(request.getNombre().trim());
        dispositivo.setTipo(request.getTipo().trim());
        dispositivo.setPotenciaWatts(newPower);
        if (request.getActivo() != null) {
            dispositivo.setActivo(request.getActivo());
        }
        if (request.getAutomatico() != null) {
            dispositivo.setAutomatico(request.getAutomatico());
        }
        dispositivo.setLimiteKwh(request.getLimiteKwh());
        dispositivo.setHabitacion(habitacion);

        DispositivoVirtual updated = dispositivoVirtualRepositorio.save(dispositivo);
        if (powerChanged) {
            recalculateConsumption(updated);
        }

        DispositivoDTO dispositivoDTO = modelMapper.map(updated, DispositivoDTO.class);
        dispositivoDTO.setStatus(updated.isActivo() ? "ON" : "OFF");
        dispositivoDTO.setMode(updated.isAutomatico() ? "AUTOMATIC" : "MANUAL");
        dispositivoDTO.setHabitacionId(updated.getHabitacion().getId());
        dispositivoDTO.setHabitacionNombre(updated.getHabitacion().getNombre());
        return dispositivoDTO;
    }

    @Transactional
    public void delete(Long id) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        dispositivo.setActivo(false);
        dispositivo.setEliminado(true);
        dispositivoVirtualRepositorio.save(dispositivo);
    }

    @Transactional
    public DispositivoDTO updateStatus(Long id, EstadoActualDispositivoDto request) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        boolean desiredOn = "ON".equals(request.getStatus());

        dispositivo.setActivo(desiredOn);
        DispositivoVirtual updated = dispositivoVirtualRepositorio.save(dispositivo);
        if (desiredOn) {
            historicoRepositorio.save(buildHistory(updated, 0, 1));
        }

        DispositivoDTO dispositivoDTO = modelMapper.map(updated, DispositivoDTO.class);
        dispositivoDTO.setStatus(updated.isActivo() ? "ON" : "OFF");
        dispositivoDTO.setMode(updated.isAutomatico() ? "AUTOMATIC" : "MANUAL");
        dispositivoDTO.setHabitacionId(updated.getHabitacion().getId());
        dispositivoDTO.setHabitacionNombre(updated.getHabitacion().getNombre());
        return dispositivoDTO;
    }

    @Transactional(readOnly = true)
    public List<DispositivoDTO> findAll(Long usuarioId) {
        return dispositivoVirtualRepositorio.findByHabitacionCasaUsuarioIdAndEliminadoFalseOrderByIdAsc(usuarioId)
                .stream()
                .map(dispositivo -> {
                    DispositivoDTO dispositivoDTO = modelMapper.map(dispositivo, DispositivoDTO.class);
                    dispositivoDTO.setStatus(dispositivo.isActivo() ? "ON" : "OFF");
                    dispositivoDTO.setMode(dispositivo.isAutomatico() ? "AUTOMATIC" : "MANUAL");
                    dispositivoDTO.setHabitacionId(dispositivo.getHabitacion().getId());
                    dispositivoDTO.setHabitacionNombre(dispositivo.getHabitacion().getNombre());
                    return dispositivoDTO;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public DispositivoDTO findById(Long id) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        DispositivoDTO dispositivoDTO = modelMapper.map(dispositivo, DispositivoDTO.class);
        dispositivoDTO.setStatus(dispositivo.isActivo() ? "ON" : "OFF");
        dispositivoDTO.setMode(dispositivo.isAutomatico() ? "AUTOMATIC" : "MANUAL");
        dispositivoDTO.setHabitacionId(dispositivo.getHabitacion().getId());
        dispositivoDTO.setHabitacionNombre(dispositivo.getHabitacion().getNombre());
        return dispositivoDTO;
    }

    @Transactional
    public DispositivoDTO updateMode(Long id, ModoDispositivoDto request) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        dispositivo.setAutomatico("AUTOMATIC".equals(request.getMode()));
        dispositivo = dispositivoVirtualRepositorio.save(dispositivo);
        DispositivoDTO dispositivoDTO = modelMapper.map(dispositivo, DispositivoDTO.class);
        dispositivoDTO.setStatus(dispositivo.isActivo() ? "ON" : "OFF");
        dispositivoDTO.setMode(dispositivo.isAutomatico() ? "AUTOMATIC" : "MANUAL");
        dispositivoDTO.setHabitacionId(dispositivo.getHabitacion().getId());
        dispositivoDTO.setHabitacionNombre(dispositivo.getHabitacion().getNombre());
        return dispositivoDTO;
    }

    private List<Historico> buildSimulatedConsumption(DispositivoVirtual dispositivo) {
        int estimatedHours = estimateDailyUsageHours(dispositivo.getTipo());

        return List.of(
                buildHistory(dispositivo, 6, Math.max(1, estimatedHours - 1)),
                buildHistory(dispositivo, 5, estimatedHours),
                buildHistory(dispositivo, 4, estimatedHours + 1),
                buildHistory(dispositivo, 3, estimatedHours),
                buildHistory(dispositivo, 2, Math.max(1, estimatedHours - 2)),
                buildHistory(dispositivo, 1, estimatedHours + 2),
                buildHistory(dispositivo, 0, estimatedHours)
        );
    }

    private Historico buildHistory(DispositivoVirtual dispositivo, int daysAgo, int hours) {
        int minutes = hours * 60;
        double kwh = (dispositivo.getPotenciaWatts() * hours) / 1000.0;

        return Historico.builder()
                .fechaRegistro(LocalDateTime.now().minusDays(daysAgo))
                .kwhConsumidos(kwh)
                .duracionMinutos(minutes)
                .dispositivo(dispositivo)
                .build();
    }

    private int estimateDailyUsageHours(String tipo) {
        String normalized = tipo == null ? "" : tipo.trim().toLowerCase();

        return switch (normalized) {
            case "refrigerador", "nevera", "frigorifico" -> 8;
            case "aire acondicionado", "ac", "climatizador" -> 6;
            case "luz", "foco", "lampara" -> 5;
            case "tv", "televisor" -> 4;
            case "computadora", "pc", "laptop" -> 6;
            default -> 3;
        };
    }

    private void recalculateConsumption(DispositivoVirtual dispositivo) {
        List<Historico> history = historicoRepositorio.findByDispositivoId(dispositivo.getId());
        if (history.isEmpty()) {
            historicoRepositorio.save(buildHistory(dispositivo, 0, estimateDailyUsageHours(dispositivo.getTipo())));
            return;
        }

        history.forEach(item -> {
            int minutes = item.getDuracionMinutos() == null ? estimateDailyUsageHours(dispositivo.getTipo()) * 60 : item.getDuracionMinutos();
            double hours = minutes / 60.0;
            item.setKwhConsumidos((dispositivo.getPotenciaWatts() * hours) / 1000.0);
        });
        historicoRepositorio.saveAll(history);
    }

    private DispositivoVirtual findActiveDevice(Long id) {
        return dispositivoVirtualRepositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));
    }

    private Habitacion findRoomInSameHome(DispositivoVirtual dispositivo, Long roomId) {
        Long casaId = dispositivo.getHabitacion().getCasa().getId();
        return habitacionRepositorio.findByIdAndCasaId(roomId, casaId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));
    }

}
