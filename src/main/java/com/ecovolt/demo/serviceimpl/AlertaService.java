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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public LimitResponseDto crearLimite(AlertLimitRequestDto solicitud, Long usuarioId) {
        return guardarLimite(solicitud.getDeviceId(), solicitud.getLimitKwh(), usuarioId);
    }

    @Transactional
    public LimitResponseDto actualizarLimite(Long dispositivoId, AlertLimitRequestDto solicitud, Long usuarioId) {
        return guardarLimite(dispositivoId, solicitud.getLimitKwh(), usuarioId);
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> obtenerHistorial(Long usuarioId) {
        return alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::convertirARespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> filtrarAlertas(Long usuarioId, Long dispositivoId, LocalDate desde, LocalDate hasta) {
        List<Alerta> alertas = alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        List<AlertResponseDto> respuesta = new ArrayList<>();

        for (Alerta alerta : alertas) {
            boolean valido = true;

            if (dispositivoId != null) {
                if (alerta.getDispositivo() == null || !alerta.getDispositivo().getId().equals(dispositivoId)) {
                    valido = false;
                }
            }

            if (desde != null && alerta.getFechaCreacion().toLocalDate().isBefore(desde)) {
                valido = false;
            }

            if (hasta != null && alerta.getFechaCreacion().toLocalDate().isAfter(hasta)) {
                valido = false;
            }

            if (valido) {
                respuesta.add(convertirARespuesta(alerta));
            }
        }

        return respuesta;
    }

    @Transactional
    public AlertResponseDto marcarComoLeida(Long alertaId, Long usuarioId) {
        Alerta alerta = alertaRepositorio.findByIdAndDispositivoHabitacionCasaUsuarioId(alertaId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));

        alerta.setLeido(true);
        return convertirARespuesta(alertaRepositorio.save(alerta));
    }

    private LimitResponseDto guardarLimite(Long dispositivoId, Double limiteKwh, Long usuarioId) {
        DispositivoVirtual dispositivo = dispositivoVirtualRepositorio.findByIdAndHabitacionCasaUsuarioId(dispositivoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));

        dispositivo.setLimiteKwh(limiteKwh);
        dispositivo = dispositivoVirtualRepositorio.save(dispositivo);
        crearAlertaSiSuperaLimite(dispositivo, usuarioId);

        return LimitResponseDto.builder()
                .deviceId(dispositivo.getId())
                .deviceName(dispositivo.getNombre())
                .limitKwh(dispositivo.getLimiteKwh())
                .build();
    }

    private void crearAlertaSiSuperaLimite(DispositivoVirtual dispositivo, Long usuarioId) {
        double consumoKwh = historicoRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(usuarioId)
                .stream()
                .filter(history -> history.getDispositivo().getId().equals(dispositivo.getId()))
                .map(Historico::getKwhConsumidos)
                .mapToDouble(Double::doubleValue)
                .sum();

        if (dispositivo.getLimiteKwh() == null || consumoKwh <= dispositivo.getLimiteKwh()) {
            return;
        }

        alertaRepositorio.save(Alerta.builder()
                .tipo(EXCESS_CONSUMPTION)
                .mensaje("El dispositivo " + dispositivo.getNombre() + " supero el limite de consumo configurado")
                .fechaCreacion(LocalDateTime.now())
                .leido(false)
                .dispositivo(dispositivo)
                .build());
    }

    private AlertResponseDto convertirARespuesta(Alerta alerta) {
        DispositivoVirtual dispositivo = alerta.getDispositivo();

        return AlertResponseDto.builder()
                .id(alerta.getId())
                .tipo(alerta.getTipo())
                .mensaje(alerta.getMensaje())
                .fechaCreacion(alerta.getFechaCreacion())
                .leido(alerta.isLeido())
                .deviceId(dispositivo == null ? null : dispositivo.getId())
                .deviceName(dispositivo == null ? null : dispositivo.getNombre())
                .build();
    }
}
