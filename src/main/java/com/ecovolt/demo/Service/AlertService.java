package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.AlertLimitRequestDto;
import com.ecovolt.demo.Dto.Response.AlertResponseDto;
import com.ecovolt.demo.Dto.Response.LimitResponseDto;
import com.ecovolt.demo.Entities.AlertaEntity;
import com.ecovolt.demo.Entities.HistoricoEntity;
import com.ecovolt.demo.Entities.VirtualDeviceEntity;
import com.ecovolt.demo.Exception.ResourceNotFoundException;
import com.ecovolt.demo.Repository.AlertaRepository;
import com.ecovolt.demo.Repository.HistoricoRepository;
import com.ecovolt.demo.Repository.VirtualDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private static final String EXCESS_CONSUMPTION = "CONSUMO_EXCESIVO";

    private final VirtualDeviceRepository virtualDeviceRepository;
    private final HistoricoRepository historicoRepository;
    private final AlertaRepository alertaRepository;

    @Transactional
    public LimitResponseDto createLimit(AlertLimitRequestDto request, Long userId) {
        return saveLimit(request.getDeviceId(), request.getLimitKwh(), userId);
    }

    @Transactional
    public LimitResponseDto updateLimit(Long deviceId, AlertLimitRequestDto request, Long userId) {
        return saveLimit(deviceId, request.getLimitKwh(), userId);
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> getHistory(Long userId) {
        return alertaRepository.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AlertResponseDto markAsRead(Long alertId, Long userId) {
        AlertaEntity alert = alertaRepository.findByIdAndDispositivoHabitacionCasaUsuarioId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));

        alert.setLeido(true);
        return toResponse(alertaRepository.save(alert));
    }

    private LimitResponseDto saveLimit(Long deviceId, Double limitKwh, Long userId) {
        VirtualDeviceEntity device = virtualDeviceRepository.findByIdAndHabitacionCasaUsuarioId(deviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));

        device.setLimiteKwh(limitKwh);
        device = virtualDeviceRepository.save(device);
        createAlertIfLimitWasExceeded(device, userId);

        return LimitResponseDto.builder()
                .deviceId(device.getId())
                .deviceName(device.getNombre())
                .limitKwh(device.getLimiteKwh())
                .build();
    }

    private void createAlertIfLimitWasExceeded(VirtualDeviceEntity device, Long userId) {
        double consumedKwh = historicoRepository.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(userId)
                .stream()
                .filter(history -> history.getDispositivo().getId().equals(device.getId()))
                .map(HistoricoEntity::getKwhConsumidos)
                .mapToDouble(Double::doubleValue)
                .sum();

        if (device.getLimiteKwh() == null || consumedKwh <= device.getLimiteKwh()) {
            return;
        }

        alertaRepository.save(AlertaEntity.builder()
                .tipo(EXCESS_CONSUMPTION)
                .mensaje("El dispositivo " + device.getNombre() + " supero el limite de consumo configurado")
                .fechaCreacion(LocalDateTime.now())
                .leido(false)
                .dispositivo(device)
                .build());
    }

    private AlertResponseDto toResponse(AlertaEntity alert) {
        VirtualDeviceEntity device = alert.getDispositivo();

        return AlertResponseDto.builder()
                .id(alert.getId())
                .tipo(alert.getTipo())
                .mensaje(alert.getMensaje())
                .fechaCreacion(alert.getFechaCreacion())
                .leido(alert.isLeido())
                .deviceId(device == null ? null : device.getId())
                .deviceName(device == null ? null : device.getNombre())
                .build();
    }
}
