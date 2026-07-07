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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertaService {

    private static final String EXCESS_CONSUMPTION = "CONSUMO_EXCESIVO";
    private static final String WARNING_CONSUMPTION = "CONSUMO_ELEVADO";
    private static final double WARNING_THRESHOLD = 0.75;

    private final DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    private final HistoricoRepositorio historicoRepositorio;
    private final AlertaRepositorio alertaRepositorio;
    private final ModelMapper modelMapper;

    public AlertaService(DispositivoVirtualRepositorio dispositivoVirtualRepositorio,
                         HistoricoRepositorio historicoRepositorio,
                         AlertaRepositorio alertaRepositorio,
                         ModelMapper modelMapper) {
        this.dispositivoVirtualRepositorio = dispositivoVirtualRepositorio;
        this.historicoRepositorio = historicoRepositorio;
        this.alertaRepositorio = alertaRepositorio;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public AlertaDTO create(AlertaDTO request, Long usuarioId) {
        DispositivoVirtual dispositivo = findDevice(request.getDeviceId(), usuarioId);
        Alerta alerta = modelMapper.map(request, Alerta.class);

        alerta.setId(null);
        alerta.setDispositivo(dispositivo);
        if (alerta.getFechaCreacion() == null) {
            alerta.setFechaCreacion(LocalDateTime.now());
        }

        return toDto(alertaRepositorio.save(alerta));
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> findAll(Long usuarioId) {
        return alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertaDTO findById(Long id, Long usuarioId) {
        return toDto(findAlert(id, usuarioId));
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
                .map(this::toDto)
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
                respuesta.add(toDto(alerta));
            }
        }

        return respuesta;
    }

    @Transactional
    public AlertaDTO marcarComoLeida(Long alertaId, Long usuarioId) {
        Alerta alerta = alertaRepositorio.findByIdAndDispositivoHabitacionCasaUsuarioId(alertaId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));

        alerta.setLeido(true);
        return toDto(alertaRepositorio.save(alerta));
    }

    @Transactional
    public AlertaDTO update(Long id, AlertaDTO request, Long usuarioId) {
        Alerta alerta = findAlert(id, usuarioId);
        DispositivoVirtual dispositivo = findDevice(request.getDeviceId(), usuarioId);
        LocalDateTime fechaActual = alerta.getFechaCreacion();

        modelMapper.map(request, alerta);
        alerta.setId(id);
        alerta.setDispositivo(dispositivo);
        if (request.getFechaCreacion() != null) {
            alerta.setFechaCreacion(request.getFechaCreacion());
        } else {
            alerta.setFechaCreacion(fechaActual);
        }

        return toDto(alertaRepositorio.save(alerta));
    }

    @Transactional
    public void delete(Long id, Long usuarioId) {
        Alerta alerta = findAlert(id, usuarioId);
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
        if (dispositivo.getLimiteKwh() == null) return;

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate hoy = LocalDate.now();

        double consumoMensualKwh = historicoRepositorio
                .findByDispositivoIdAndDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(
                        dispositivo.getId(), usuarioId)
                .stream()
                .filter(h -> h.getKwhConsumidos() != null)
                .filter(h -> {
                    LocalDate fecha = h.getFechaRegistro().toLocalDate();
                    return !fecha.isBefore(inicioMes) && !fecha.isAfter(hoy);
                })
                .mapToDouble(Historico::getKwhConsumidos)
                .sum();

        double limite = dispositivo.getLimiteKwh();
        double ratio = consumoMensualKwh / limite;

        LocalDateTime inicioPeriodo = inicioMes.atStartOfDay();
        LocalDateTime finPeriodo = LocalDateTime.now();

        if (ratio > 1.0) {
            if (!yaExisteAlertaEnPeriodo(dispositivo.getId(), EXCESS_CONSUMPTION, inicioPeriodo, finPeriodo)) {
                guardarAlerta(EXCESS_CONSUMPTION,
                        "El dispositivo " + dispositivo.getNombre()
                                + " superó el límite mensual de " + limite
                                + " kWh (consumo actual: " + String.format("%.1f", consumoMensualKwh) + " kWh)",
                        dispositivo);
            }
        } else if (ratio >= WARNING_THRESHOLD) {
            if (!yaExisteAlertaEnPeriodo(dispositivo.getId(), WARNING_CONSUMPTION, inicioPeriodo, finPeriodo)) {
                guardarAlerta(WARNING_CONSUMPTION,
                        "El dispositivo " + dispositivo.getNombre()
                                + " alcanzó el " + String.format("%.0f", ratio * 100)
                                + "% del límite mensual ("
                                + String.format("%.1f", consumoMensualKwh) + " / " + limite + " kWh)",
                        dispositivo);
            }
        }
    }

    // Evita recrear la misma alerta dentro del mismo periodo mensual, aunque el usuario ya la
    // haya marcado como leída (mismo criterio que SimuladorConsumoService).
    private boolean yaExisteAlertaEnPeriodo(Long dispositivoId, String tipo, LocalDateTime inicio, LocalDateTime fin) {
        return alertaRepositorio.existsByDispositivoIdAndTipoAndLeidoFalse(dispositivoId, tipo)
                || alertaRepositorio.existsByDispositivoIdAndTipoAndFechaCreacionBetween(dispositivoId, tipo, inicio, fin);
    }

    private void guardarAlerta(String tipo, String mensaje, DispositivoVirtual dispositivo) {
        alertaRepositorio.save(Alerta.builder()
                .tipo(tipo)
                .mensaje(mensaje)
                .fechaCreacion(LocalDateTime.now())
                .leido(false)
                .dispositivo(dispositivo)
                .build());
    }

    private AlertaDTO toDto(Alerta alerta) {
        AlertaDTO dto = modelMapper.map(alerta, AlertaDTO.class);
        dto.setDeviceId(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getId());
        dto.setDeviceName(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getNombre());
        return dto;
    }

    private Alerta findAlert(Long id, Long usuarioId) {
        return alertaRepositorio.findByIdAndDispositivoHabitacionCasaUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));
    }

    private DispositivoVirtual findDevice(Long dispositivoId, Long usuarioId) {
        if (dispositivoId == null) {
            return null;
        }

        return dispositivoVirtualRepositorio.findByIdAndHabitacionCasaUsuarioId(dispositivoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));
    }

}
