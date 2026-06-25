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

        alerta = alertaRepositorio.save(alerta);
        AlertaDTO alertaDTO = modelMapper.map(alerta, AlertaDTO.class);
        alertaDTO.setDeviceId(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getId());
        alertaDTO.setDeviceName(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getNombre());
        return alertaDTO;
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> findAll(Long usuarioId) {
        return alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(alerta -> {
                    AlertaDTO alertaDTO = modelMapper.map(alerta, AlertaDTO.class);
                    alertaDTO.setDeviceId(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getId());
                    alertaDTO.setDeviceName(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getNombre());
                    return alertaDTO;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertaDTO findById(Long id, Long usuarioId) {
        Alerta alerta = findAlert(id, usuarioId);
        AlertaDTO alertaDTO = modelMapper.map(alerta, AlertaDTO.class);
        alertaDTO.setDeviceId(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getId());
        alertaDTO.setDeviceName(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getNombre());
        return alertaDTO;
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
                .map(alerta -> {
                    AlertaDTO alertaDTO = modelMapper.map(alerta, AlertaDTO.class);
                    alertaDTO.setDeviceId(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getId());
                    alertaDTO.setDeviceName(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getNombre());
                    return alertaDTO;
                })
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
                AlertaDTO alertaDTO = modelMapper.map(alerta, AlertaDTO.class);
                alertaDTO.setDeviceId(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getId());
                alertaDTO.setDeviceName(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getNombre());
                respuesta.add(alertaDTO);
            }
        }

        return respuesta;
    }

    @Transactional
    public AlertaDTO marcarComoLeida(Long alertaId, Long usuarioId) {
        Alerta alerta = alertaRepositorio.findByIdAndDispositivoHabitacionCasaUsuarioId(alertaId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));

        alerta.setLeido(true);
        alerta = alertaRepositorio.save(alerta);
        AlertaDTO alertaDTO = modelMapper.map(alerta, AlertaDTO.class);
        alertaDTO.setDeviceId(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getId());
        alertaDTO.setDeviceName(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getNombre());
        return alertaDTO;
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

        alerta = alertaRepositorio.save(alerta);
        AlertaDTO alertaDTO = modelMapper.map(alerta, AlertaDTO.class);
        alertaDTO.setDeviceId(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getId());
        alertaDTO.setDeviceName(alerta.getDispositivo() == null ? null : alerta.getDispositivo().getNombre());
        return alertaDTO;
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
