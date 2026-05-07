package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.LimiteAlertaSolicitudDto;
import com.ecovolt.demo.dtos.AlertaDTO;
import com.ecovolt.demo.dtos.LimiteRespuestaDto;
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

    private final DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    private final HistoricoRepositorio historicoRepositorio;
    private final AlertaRepositorio alertaRepositorio;

    public AlertaService(DispositivoVirtualRepositorio dispositivoVirtualRepositorio,
                         HistoricoRepositorio historicoRepositorio,
                         AlertaRepositorio alertaRepositorio) {
        this.dispositivoVirtualRepositorio = dispositivoVirtualRepositorio;
        this.historicoRepositorio = historicoRepositorio;
        this.alertaRepositorio = alertaRepositorio;
    }

    @Transactional
    public AlertaDTO create(Alerta request) {
        DispositivoVirtual dispositivo = findDevice(request);

        request.setId(null);
        request.setDispositivo(dispositivo);
        if (request.getFechaCreacion() == null) {
            request.setFechaCreacion(LocalDateTime.now());
        }

        return convertirARespuesta(alertaRepositorio.save(request));
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> findAll() {
        return alertaRepositorio.findAll()
                .stream()
                .map(this::convertirARespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertaDTO findById(Long id) {
        return convertirARespuesta(findAlert(id));
    }

    @Transactional
    public LimiteRespuestaDto crearLimite(LimiteAlertaSolicitudDto solicitud, Long usuarioId) {
        return guardarLimite(solicitud.getDeviceId(), solicitud.getLimitKwh(), usuarioId);
    }

    @Transactional
    public LimiteRespuestaDto actualizarLimite(Long dispositivoId, LimiteAlertaSolicitudDto solicitud, Long usuarioId) {
        return guardarLimite(dispositivoId, solicitud.getLimitKwh(), usuarioId);
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> obtenerHistorial(Long usuarioId) {
        return alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::convertirARespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> filtrarAlertas(Long usuarioId, Long dispositivoId, LocalDate desde, LocalDate hasta) {
        List<Alerta> alertas = alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        List<AlertaDTO> respuesta = new ArrayList<>();

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
    public AlertaDTO marcarComoLeida(Long alertaId, Long usuarioId) {
        Alerta alerta = alertaRepositorio.findByIdAndDispositivoHabitacionCasaUsuarioId(alertaId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));

        alerta.setLeido(true);
        return convertirARespuesta(alertaRepositorio.save(alerta));
    }

    @Transactional
    public AlertaDTO update(Long id, Alerta request) {
        Alerta alerta = findAlert(id);
        DispositivoVirtual dispositivo = findDevice(request);

        alerta.setTipo(request.getTipo());
        alerta.setMensaje(request.getMensaje());
        alerta.setLeido(request.isLeido());
        alerta.setDispositivo(dispositivo);
        if (request.getFechaCreacion() != null) {
            alerta.setFechaCreacion(request.getFechaCreacion());
        }

        return convertirARespuesta(alertaRepositorio.save(alerta));
    }

    @Transactional
    public void delete(Long id) {
        Alerta alerta = findAlert(id);
        alertaRepositorio.delete(alerta);
    }

    private LimiteRespuestaDto guardarLimite(Long dispositivoId, Double limiteKwh, Long usuarioId) {
        DispositivoVirtual dispositivo = dispositivoVirtualRepositorio.findByIdAndHabitacionCasaUsuarioId(dispositivoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));

        dispositivo.setLimiteKwh(limiteKwh);
        dispositivo = dispositivoVirtualRepositorio.save(dispositivo);
        crearAlertaSiSuperaLimite(dispositivo, usuarioId);

        return LimiteRespuestaDto.builder()
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

    private Alerta findAlert(Long id) {
        return alertaRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));
    }

    private DispositivoVirtual findDevice(Alerta alerta) {
        if (alerta.getDispositivo() == null || alerta.getDispositivo().getId() == null) {
            return null;
        }

        return dispositivoVirtualRepositorio.findById(alerta.getDispositivo().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));
    }

    private AlertaDTO convertirARespuesta(Alerta alerta) {
        DispositivoVirtual dispositivo = alerta.getDispositivo();

        return AlertaDTO.builder()
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
