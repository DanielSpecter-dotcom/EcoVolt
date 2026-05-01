package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.DeviceCreateDto;
import com.ecovolt.demo.Dto.Response.DeviceResponseDto;
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
        dispositivo.setHabitacion(habitacion);
        dispositivo = virtualDeviceRepository.save(dispositivo);

        historicoRepository.saveAll(buildSimulatedConsumption(dispositivo));

        return modelMapper.map(dispositivo, DeviceResponseDto.class);
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
}
