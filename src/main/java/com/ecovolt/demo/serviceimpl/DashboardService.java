package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.request.RoutineUpdateRequestDto;
import com.ecovolt.demo.dtos.response.ActividadDashboardDto;
import com.ecovolt.demo.dtos.response.DispositivoDashboardDto;
import com.ecovolt.demo.dtos.response.EscenaRutinaDashboardDto;
import com.ecovolt.demo.dtos.response.EscenasRutinasDashboardDto;
import com.ecovolt.demo.dtos.response.ResumenDashboardDto;
import com.ecovolt.demo.dtos.response.RoutineResponseDto;
import com.ecovolt.demo.dtos.response.SceneActivationResponseDto;
import com.ecovolt.demo.dtos.response.SceneResponseDto;
import com.ecovolt.demo.entities.Alerta;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.repositories.AlertaRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import com.ecovolt.demo.services.RoutineService;
import com.ecovolt.demo.services.SceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private static final double COSTO_KWH_SOLES = 0.65;

    @Autowired
    private HistoricoRepositorio historicoRepositorio;

    @Autowired
    private DispositivoVirtualRepositorio dispositivoVirtualRepositorio;

    @Autowired
    private AlertaRepositorio alertaRepositorio;

    @Autowired
    private SceneService escenaService;

    @Autowired
    private RoutineService rutinaService;

    @Transactional(readOnly = true)
    public ResumenDashboardDto obtenerResumen(Long usuarioId) {
        List<Historico> historial = historicoRepositorio
                .findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(usuarioId);

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMes.minusMonths(1);
        LocalDate finMesAnterior = inicioMes.minusDays(1);

        double consumoDiario = 0;
        double consumoMensual = 0;
        double consumoMesAnterior = 0;

        for (Historico registro : historial) {
            LocalDate fecha = registro.getFechaRegistro().toLocalDate();

            if (fecha.isEqual(hoy)) {
                consumoDiario = consumoDiario + registro.getKwhConsumidos();
            }

            if (!fecha.isBefore(inicioMes) && !fecha.isAfter(hoy)) {
                consumoMensual = consumoMensual + registro.getKwhConsumidos();
            }

            if (!fecha.isBefore(inicioMesAnterior) && !fecha.isAfter(finMesAnterior)) {
                consumoMesAnterior = consumoMesAnterior + registro.getKwhConsumidos();
            }
        }

        double variacion = calcularVariacion(consumoMensual, consumoMesAnterior);
        double costo = consumoMensual * COSTO_KWH_SOLES;

        return new ResumenDashboardDto(
                redondear(consumoDiario),
                redondear(consumoMensual),
                redondear(costo),
                redondear(variacion)
        );
    }

    @Transactional(readOnly = true)
    public List<DispositivoDashboardDto> obtenerDispositivos(Long usuarioId) {
        List<DispositivoVirtual> dispositivos = dispositivoVirtualRepositorio.findByHabitacionCasaUsuarioId(usuarioId);
        List<DispositivoDashboardDto> respuesta = new ArrayList<>();

        for (DispositivoVirtual dispositivo : dispositivos) {
            if (!dispositivo.isEliminado()) {
                String estado = dispositivo.isActivo() ? "encendido" : "apagado";
                String ubicacion = dispositivo.getHabitacion() == null ? "Sin habitacion" : dispositivo.getHabitacion().getNombre();

                respuesta.add(new DispositivoDashboardDto(
                        dispositivo.getId(),
                        dispositivo.getNombre(),
                        dispositivo.getTipo(),
                        ubicacion,
                        estado,
                        dispositivo.isActivo()
                ));
            }
        }

        return respuesta;
    }

    public EscenasRutinasDashboardDto obtenerEscenasRutinas() {
        List<EscenaRutinaDashboardDto> escenas = new ArrayList<>();
        List<EscenaRutinaDashboardDto> rutinas = new ArrayList<>();

        for (SceneResponseDto escena : escenaService.findAll()) {
            escenas.add(new EscenaRutinaDashboardDto(
                    escena.getId(),
                    escena.getName(),
                    "escena",
                    "disponible"
            ));
        }

        for (RoutineResponseDto rutina : rutinaService.findAll()) {
            String estado = obtenerEstadoRutina(rutina);
            rutinas.add(new EscenaRutinaDashboardDto(
                    rutina.getId(),
                    rutina.getName(),
                    "rutina",
                    estado
            ));
        }

        return new EscenasRutinasDashboardDto(escenas, rutinas);
    }

    public SceneActivationResponseDto activarEscena(Long escenaId) {
        return escenaService.activate(escenaId);
    }

    public RoutineResponseDto pausarRutina(Long rutinaId) {
        RoutineUpdateRequestDto request = new RoutineUpdateRequestDto();
        request.setHabilitar(false);
        return rutinaService.update(rutinaId, request);
    }

    @Transactional(readOnly = true)
    public List<ActividadDashboardDto> obtenerActividad(Long usuarioId) {
        List<ActividadDashboardDto> actividades = new ArrayList<>();

        List<Historico> historial = historicoRepositorio
                .findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(usuarioId);
        for (Historico registro : historial) {
            DispositivoVirtual dispositivo = registro.getDispositivo();
            String nombre = dispositivo.getNombre();
            String tipo = dispositivo.isAutomatico() ? "automatica" : "manual";
            String descripcion = "Consumo registrado en " + nombre + ": " + redondear(registro.getKwhConsumidos()) + " kWh";
            actividades.add(new ActividadDashboardDto(registro.getFechaRegistro(), descripcion, tipo));
        }

        List<Alerta> alertas = alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        for (Alerta alerta : alertas) {
            actividades.add(new ActividadDashboardDto(alerta.getFechaCreacion(), alerta.getMensaje(), "alerta"));
        }

        actividades.sort(Comparator.comparing(ActividadDashboardDto::getHora).reversed());

        if (actividades.size() > 10) {
            return actividades.subList(0, 10);
        }
        return actividades;
    }

    private String obtenerEstadoRutina(RoutineResponseDto rutina) {
        if (Boolean.TRUE.equals(rutina.getPausedByAwayMode())) {
            return "pausada";
        }
        if (Boolean.FALSE.equals(rutina.getEnabled())) {
            return "pausada";
        }
        return "activa";
    }

    private double calcularVariacion(double actual, double anterior) {
        if (anterior == 0) {
            if (actual == 0) {
                return 0;
            }
            return 100;
        }
        return ((actual - anterior) / anterior) * 100;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
