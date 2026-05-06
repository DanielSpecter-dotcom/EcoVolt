package com.ecovolt.demo.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.AlertLimitRequestDto;
import com.ecovolt.demo.dtos.response.AlertResponseDto;
import com.ecovolt.demo.dtos.response.LimitResponseDto;
import com.ecovolt.demo.entities.Alerta;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.AlertaRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertaService {

    private static final String EXCESS_CONSUMPTION = "CONSUMO_EXCESIVO";

    @Autowired
    private DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    @Autowired
    private HistoricoRepositorio historicoRepositorio;
    @Autowired
    private AlertaRepositorio alertaRepositorio;

    @Transactional
    public LimitResponseDto crearLimite(AlertLimitRequestDto request, Long userId) {
        return saveLimit(request.getDeviceId(), request.getLimitKwh(), userId);
    }

    @Transactional
    public LimitResponseDto updateLimit(Long deviceId, AlertLimitRequestDto request, Long userId) {
        return saveLimit(deviceId, request.getLimitKwh(), userId);
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> getHistory(Long userId) {
        return alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AlertResponseDto markAsRead(Long alertId, Long userId) {
        Alerta alert = alertaRepositorio.findByIdAndDispositivoHabitacionCasaUsuarioId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));

        alert.setLeido(true);
        return toResponse(alertaRepositorio.save(alert));
    }

    private LimitResponseDto saveLimit(Long deviceId, Double limitKwh, Long userId) {
        DispositivoVirtual device = dispositivoVirtualRepositorio.findByIdAndHabitacionCasaUsuarioId(deviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));

        device.setLimiteKwh(limitKwh);
        device = dispositivoVirtualRepositorio.save(device);
        createAlertIfLimitWasExceeded(device, userId);

        return LimitResponseDto.builder()
                .deviceId(device.getId())
                .deviceName(device.getNombre())
                .limitKwh(device.getLimiteKwh())
                .build();
    }

    private void createAlertIfLimitWasExceeded(DispositivoVirtual device, Long userId) {
        double consumedKwh = historicoRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(userId)
                .stream()
                .filter(history -> history.getDispositivo().getId().equals(device.getId()))
                .map(Historico::getKwhConsumidos)
                .mapToDouble(Double::doubleValue)
                .sum();

        if (device.getLimiteKwh() == null || consumedKwh <= device.getLimiteKwh()) {
            return;
        }

        alertaRepositorio.save(Alerta.builder()
                .tipo(EXCESS_CONSUMPTION)
                .mensaje("El dispositivo " + device.getNombre() + " supero el limite de consumo configurado")
                .fechaCreacion(LocalDateTime.now())
                .leido(false)
                .dispositivo(device)
                .build());
    }

    private AlertResponseDto toResponse(Alerta alert) {
        DispositivoVirtual device = alert.getDispositivo();

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
