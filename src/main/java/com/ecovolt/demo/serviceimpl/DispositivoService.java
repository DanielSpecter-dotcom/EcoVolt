package com.ecovolt.demo.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.DeviceCreateDto;
import com.ecovolt.demo.dtos.request.DeviceModeRequestDto;
import com.ecovolt.demo.dtos.request.DeviceRoomRequestDto;
import com.ecovolt.demo.dtos.request.DeviceStatusRequestDto;
import com.ecovolt.demo.dtos.request.DeviceUpdateRequestDto;
import com.ecovolt.demo.dtos.request.RoomCreateRequestDto;
import com.ecovolt.demo.dtos.response.DeviceResponseDto;
import com.ecovolt.demo.dtos.response.RoomResponseDto;
import com.ecovolt.demo.entities.Casa;
import com.ecovolt.demo.entities.Habitacion;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.entities.Usuario;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.CasaRepositorio;
import com.ecovolt.demo.repositories.HabitacionRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import com.ecovolt.demo.repositories.UsuarioRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DispositivoService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private CasaRepositorio casaRepositorio;
    @Autowired
    private HabitacionRepositorio habitacionRepositorio;
    @Autowired
    private DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    @Autowired
    private HistoricoRepositorio historicoRepositorio;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public DeviceResponseDto create(DeviceCreateDto request) {
        Usuario usuario = usuarioRepositorio.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Casa casa = casaRepositorio.findFirstByUsuarioIdOrderByIdAsc(usuario.getId())
                .orElseGet(() -> casaRepositorio.save(Casa.builder()
                        .nombre("Hogar virtual")
                        .usuario(usuario)
                        .build()));

        Habitacion habitacion = habitacionRepositorio.findFirstByCasaIdOrderByIdAsc(casa.getId())
                .orElseGet(() -> habitacionRepositorio.save(Habitacion.builder()
                        .nombre("General")
                        .casa(casa)
                        .build()));

        DispositivoVirtual dispositivo = modelMapper.map(request, DispositivoVirtual.class);
        dispositivo.setNombre(buildDeviceName(request.getTipoDispositivo()));
        dispositivo.setTipo(request.getTipoDispositivo().trim());
        dispositivo.setActivo(false);
        dispositivo.setAutomatico(false);
        dispositivo.setEliminado(false);
        dispositivo.setHabitacion(habitacion);
        dispositivo = dispositivoVirtualRepositorio.save(dispositivo);

        historicoRepositorio.saveAll(buildSimulatedConsumption(dispositivo));

        return toDeviceResponse(dispositivo);
    }

    @Transactional
    public RoomResponseDto createRoom(RoomCreateRequestDto request) {
        Usuario usuario = usuarioRepositorio.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Casa casa = casaRepositorio.findFirstByUsuarioIdOrderByIdAsc(usuario.getId())
                .orElseGet(() -> casaRepositorio.save(Casa.builder()
                        .nombre("Hogar virtual")
                        .usuario(usuario)
                        .build()));

        Habitacion habitacion = habitacionRepositorio.save(Habitacion.builder()
                .nombre(request.getName().trim())
                .casa(casa)
                .build());

        return toRoomResponse(habitacion);
    }

    @Transactional
    public DeviceResponseDto assignRoom(Long id, DeviceRoomRequestDto request) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        Habitacion habitacion = findRoomInSameHome(dispositivo, request.getRoomId());

        dispositivo.setHabitacion(habitacion);
        return toDeviceResponse(dispositivoVirtualRepositorio.save(dispositivo));
    }

    @Transactional
    public DeviceResponseDto update(Long id, DeviceUpdateRequestDto request) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        Habitacion habitacion = findRoomInSameHome(dispositivo, request.getRoomId());
        boolean powerChanged = Double.compare(dispositivo.getPotenciaWatts(), request.getPower()) != 0;

        dispositivo.setNombre(request.getName().trim());
        dispositivo.setTipo(request.getType().trim());
        dispositivo.setPotenciaWatts(request.getPower());
        dispositivo.setHabitacion(habitacion);

        DispositivoVirtual updated = dispositivoVirtualRepositorio.save(dispositivo);
        if (powerChanged) {
            recalculateConsumption(updated);
        }

        return toDeviceResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        dispositivo.setActivo(false);
        dispositivo.setEliminado(true);
        dispositivoVirtualRepositorio.save(dispositivo);
    }

    @Transactional
    public DeviceResponseDto updateStatus(Long id, DeviceStatusRequestDto request) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        boolean desiredOn = "ON".equals(request.getStatus());

        dispositivo.setActivo(desiredOn);
        DispositivoVirtual updated = dispositivoVirtualRepositorio.save(dispositivo);
        if (desiredOn) {
            historicoRepositorio.save(buildHistory(updated, 0, 1));
        }

        return toDeviceResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponseDto> findAll() {
        return dispositivoVirtualRepositorio.findByEliminadoFalseOrderByIdAsc()
                .stream()
                .map(this::toDeviceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceResponseDto findById(Long id) {
        return toDeviceResponse(findActiveDevice(id));
    }

    @Transactional
    public DeviceResponseDto updateMode(Long id, DeviceModeRequestDto request) {
        DispositivoVirtual dispositivo = findActiveDevice(id);
        dispositivo.setAutomatico("AUTOMATIC".equals(request.getMode()));
        return toDeviceResponse(dispositivoVirtualRepositorio.save(dispositivo));
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

    private String buildDeviceName(String tipo) {
        String normalized = tipo == null ? "Dispositivo" : tipo.trim();
        if (normalized.isEmpty()) {
            return "Dispositivo virtual";
        }
        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1) + " virtual";
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

    private DeviceResponseDto toDeviceResponse(DispositivoVirtual dispositivo) {
        Habitacion habitacion = dispositivo.getHabitacion();
        return DeviceResponseDto.builder()
                .id(dispositivo.getId())
                .nombre(dispositivo.getNombre())
                .tipo(dispositivo.getTipo())
                .potenciaWatts(dispositivo.getPotenciaWatts())
                .status(dispositivo.isActivo() ? "ON" : "OFF")
                .mode(dispositivo.isAutomatico() ? "AUTOMATIC" : "MANUAL")
                .habitacionId(habitacion.getId())
                .habitacionNombre(habitacion.getNombre())
                .build();
    }

    private RoomResponseDto toRoomResponse(Habitacion habitacion) {
        return RoomResponseDto.builder()
                .id(habitacion.getId())
                .name(habitacion.getNombre())
                .homeId(habitacion.getCasa().getId())
                .build();
    }
}
