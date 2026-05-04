package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.DeviceCreateDto;
import com.ecovolt.demo.Dto.Request.DeviceModeRequestDto;
import com.ecovolt.demo.Dto.Request.DeviceRoomRequestDto;
import com.ecovolt.demo.Dto.Request.DeviceStatusRequestDto;
import com.ecovolt.demo.Dto.Request.DeviceUpdateRequestDto;
import com.ecovolt.demo.Dto.Request.RoomCreateRequestDto;
import com.ecovolt.demo.Dto.Response.DeviceResponseDto;
import com.ecovolt.demo.Dto.Response.RoomResponseDto;
import com.ecovolt.demo.Entities.CasaEntity;
import com.ecovolt.demo.Entities.HabitacionEntity;
import com.ecovolt.demo.Entities.HistoricoEntity;
import com.ecovolt.demo.Entities.UsuarioEntity;
import com.ecovolt.demo.Entities.VirtualDeviceEntity;
import com.ecovolt.demo.Exception.ResourceNotFoundException;
import com.ecovolt.demo.Repository.CasaRepository;
import com.ecovolt.demo.Repository.HabitacionRepository;
import com.ecovolt.demo.Repository.HistoricoRepository;
import com.ecovolt.demo.Repository.UsuarioRepository;
import com.ecovolt.demo.Repository.VirtualDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final UsuarioRepository usuarioRepository;
    private final CasaRepository casaRepository;
    private final HabitacionRepository habitacionRepository;
    private final VirtualDeviceRepository virtualDeviceRepository;
    private final HistoricoRepository historicoRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public DeviceResponseDto create(DeviceCreateDto request) {
        UsuarioEntity usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        CasaEntity casa = casaRepository.findFirstByUsuarioIdOrderByIdAsc(usuario.getId())
                .orElseGet(() -> casaRepository.save(CasaEntity.builder()
                        .nombre("Hogar virtual")
                        .usuario(usuario)
                        .build()));

        HabitacionEntity habitacion = habitacionRepository.findFirstByCasaIdOrderByIdAsc(casa.getId())
                .orElseGet(() -> habitacionRepository.save(HabitacionEntity.builder()
                        .nombre("General")
                        .casa(casa)
                        .build()));

        VirtualDeviceEntity dispositivo = modelMapper.map(request, VirtualDeviceEntity.class);
        dispositivo.setNombre(buildDeviceName(request.getTipoDispositivo()));
        dispositivo.setTipo(request.getTipoDispositivo().trim());
        dispositivo.setActivo(false);
        dispositivo.setAutomatico(false);
        dispositivo.setEliminado(false);
        dispositivo.setHabitacion(habitacion);
        dispositivo = virtualDeviceRepository.save(dispositivo);

        historicoRepository.saveAll(buildSimulatedConsumption(dispositivo));

        return toDeviceResponse(dispositivo);
    }

    @Transactional
    public RoomResponseDto createRoom(RoomCreateRequestDto request) {
        UsuarioEntity usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        CasaEntity casa = casaRepository.findFirstByUsuarioIdOrderByIdAsc(usuario.getId())
                .orElseGet(() -> casaRepository.save(CasaEntity.builder()
                        .nombre("Hogar virtual")
                        .usuario(usuario)
                        .build()));

        HabitacionEntity habitacion = habitacionRepository.save(HabitacionEntity.builder()
                .nombre(request.getName().trim())
                .casa(casa)
                .build());

        return toRoomResponse(habitacion);
    }

    @Transactional
    public DeviceResponseDto assignRoom(Long id, DeviceRoomRequestDto request) {
        VirtualDeviceEntity dispositivo = findActiveDevice(id);
        HabitacionEntity habitacion = findRoomInSameHome(dispositivo, request.getRoomId());

        dispositivo.setHabitacion(habitacion);
        return toDeviceResponse(virtualDeviceRepository.save(dispositivo));
    }

    @Transactional
    public DeviceResponseDto update(Long id, DeviceUpdateRequestDto request) {
        VirtualDeviceEntity dispositivo = findActiveDevice(id);
        HabitacionEntity habitacion = findRoomInSameHome(dispositivo, request.getRoomId());
        boolean powerChanged = Double.compare(dispositivo.getPotenciaWatts(), request.getPower()) != 0;

        dispositivo.setNombre(request.getName().trim());
        dispositivo.setTipo(request.getType().trim());
        dispositivo.setPotenciaWatts(request.getPower());
        dispositivo.setHabitacion(habitacion);

        VirtualDeviceEntity updated = virtualDeviceRepository.save(dispositivo);
        if (powerChanged) {
            recalculateConsumption(updated);
        }

        return toDeviceResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        VirtualDeviceEntity dispositivo = findActiveDevice(id);
        dispositivo.setActivo(false);
        dispositivo.setEliminado(true);
        virtualDeviceRepository.save(dispositivo);
    }

    @Transactional
    public DeviceResponseDto updateStatus(Long id, DeviceStatusRequestDto request) {
        VirtualDeviceEntity dispositivo = findActiveDevice(id);
        boolean desiredOn = "ON".equals(request.getStatus());

        dispositivo.setActivo(desiredOn);
        VirtualDeviceEntity updated = virtualDeviceRepository.save(dispositivo);
        if (desiredOn) {
            historicoRepository.save(buildHistory(updated, 0, 1));
        }

        return toDeviceResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponseDto> findAll() {
        return virtualDeviceRepository.findByEliminadoFalseOrderByIdAsc()
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
        VirtualDeviceEntity dispositivo = findActiveDevice(id);
        dispositivo.setAutomatico("AUTOMATIC".equals(request.getMode()));
        return toDeviceResponse(virtualDeviceRepository.save(dispositivo));
    }

    private List<HistoricoEntity> buildSimulatedConsumption(VirtualDeviceEntity dispositivo) {
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

    private HistoricoEntity buildHistory(VirtualDeviceEntity dispositivo, int daysAgo, int hours) {
        int minutes = hours * 60;
        double kwh = (dispositivo.getPotenciaWatts() * hours) / 1000.0;

        return HistoricoEntity.builder()
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

    private void recalculateConsumption(VirtualDeviceEntity dispositivo) {
        List<HistoricoEntity> history = historicoRepository.findByDispositivoId(dispositivo.getId());
        if (history.isEmpty()) {
            historicoRepository.save(buildHistory(dispositivo, 0, estimateDailyUsageHours(dispositivo.getTipo())));
            return;
        }

        history.forEach(item -> {
            int minutes = item.getDuracionMinutos() == null ? estimateDailyUsageHours(dispositivo.getTipo()) * 60 : item.getDuracionMinutos();
            double hours = minutes / 60.0;
            item.setKwhConsumidos((dispositivo.getPotenciaWatts() * hours) / 1000.0);
        });
        historicoRepository.saveAll(history);
    }

    private VirtualDeviceEntity findActiveDevice(Long id) {
        return virtualDeviceRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));
    }

    private HabitacionEntity findRoomInSameHome(VirtualDeviceEntity dispositivo, Long roomId) {
        Long casaId = dispositivo.getHabitacion().getCasa().getId();
        return habitacionRepository.findByIdAndCasaId(roomId, casaId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));
    }

    private DeviceResponseDto toDeviceResponse(VirtualDeviceEntity dispositivo) {
        HabitacionEntity habitacion = dispositivo.getHabitacion();
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

    private RoomResponseDto toRoomResponse(HabitacionEntity habitacion) {
        return RoomResponseDto.builder()
                .id(habitacion.getId())
                .name(habitacion.getNombre())
                .homeId(habitacion.getCasa().getId())
                .build();
    }
}
