package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.ActualizarRutinaDto;
import com.ecovolt.demo.dtos.ActividadPanelDto;
import com.ecovolt.demo.dtos.DispositivoPanelDto;
import com.ecovolt.demo.dtos.EscenaRutinaPanelDto;
import com.ecovolt.demo.dtos.EscenasRutinasPanelDto;
import com.ecovolt.demo.dtos.ResumenPanelDto;
import com.ecovolt.demo.dtos.RutinaDTO;
import com.ecovolt.demo.dtos.ActivacionEscenaDTO;
import com.ecovolt.demo.dtos.EscenaDTO;
import com.ecovolt.demo.entities.Alerta;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.repositories.AlertaRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import com.ecovolt.demo.services.RoutineService;
import com.ecovolt.demo.services.SceneService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private static final double COSTO_KWH_SOLES = 0.65;

    private final HistoricoRepositorio historicoRepositorio;
    private final DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    private final AlertaRepositorio alertaRepositorio;
    private final SceneService escenaService;
    private final RoutineService rutinaService;

    public DashboardService(HistoricoRepositorio historicoRepositorio,
                            DispositivoVirtualRepositorio dispositivoVirtualRepositorio,
                            AlertaRepositorio alertaRepositorio,
                            SceneService escenaService,
                            RoutineService rutinaService) {
        this.historicoRepositorio = historicoRepositorio;
        this.dispositivoVirtualRepositorio = dispositivoVirtualRepositorio;
        this.alertaRepositorio = alertaRepositorio;
        this.escenaService = escenaService;
        this.rutinaService = rutinaService;
    }

    @Transactional(readOnly = true)
    public ResumenPanelDto obtenerResumen(Long usuarioId) {
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

        return new ResumenPanelDto(
                redondear(consumoDiario),
                redondear(consumoMensual),
                redondear(costo),
                redondear(variacion)
        );
    }

    @Transactional(readOnly = true)
    public List<DispositivoPanelDto> obtenerDispositivos(Long usuarioId) {
        List<DispositivoVirtual> dispositivos = dispositivoVirtualRepositorio.findByHabitacionCasaUsuarioId(usuarioId);
        List<DispositivoPanelDto> respuesta = new ArrayList<>();

        for (DispositivoVirtual dispositivo : dispositivos) {
            if (!dispositivo.isEliminado()) {
                String estado = dispositivo.isActivo() ? "encendido" : "apagado";
                String ubicacion = dispositivo.getHabitacion() == null ? "Sin habitacion" : dispositivo.getHabitacion().getNombre();

                respuesta.add(new DispositivoPanelDto(
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

    public EscenasRutinasPanelDto obtenerEscenasRutinas(Long usuarioId) {
        List<EscenaRutinaPanelDto> escenas = new ArrayList<>();
        List<EscenaRutinaPanelDto> rutinas = new ArrayList<>();

        for (EscenaDTO escena : escenaService.findAll(usuarioId)) {
            escenas.add(new EscenaRutinaPanelDto(
                    escena.getId(),
                    escena.getName(),
                    "escena",
                    "disponible"
            ));
        }

        for (RutinaDTO rutina : rutinaService.findAll(usuarioId)) {
            String estado = obtenerEstadoRutina(rutina);
            rutinas.add(new EscenaRutinaPanelDto(
                    rutina.getId(),
                    rutina.getName(),
                    "rutina",
                    estado
            ));
        }

        return new EscenasRutinasPanelDto(escenas, rutinas);
    }

    public ActivacionEscenaDTO activarEscena(Long escenaId) {
        return escenaService.activate(escenaId);
    }

    public RutinaDTO pausarRutina(Long rutinaId, Long usuarioId) {
        ActualizarRutinaDto request = new ActualizarRutinaDto();
        request.setHabilitar(false);
        return rutinaService.update(rutinaId, request, usuarioId);
    }

    @Transactional(readOnly = true)
    public List<ActividadPanelDto> obtenerActividad(Long usuarioId) {
        List<ActividadPanelDto> actividades = new ArrayList<>();

        List<Historico> historial = historicoRepositorio
                .findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(usuarioId);
        for (Historico registro : historial) {
            DispositivoVirtual dispositivo = registro.getDispositivo();
            String nombre = dispositivo.getNombre();
            String tipo = dispositivo.isAutomatico() ? "automatica" : "manual";
            String descripcion = "Consumo registrado en " + nombre + ": " + redondear(registro.getKwhConsumidos()) + " kWh";
            actividades.add(new ActividadPanelDto(registro.getFechaRegistro(), descripcion, tipo));
        }

        List<Alerta> alertas = alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        for (Alerta alerta : alertas) {
            actividades.add(new ActividadPanelDto(alerta.getFechaCreacion(), alerta.getMensaje(), "alerta"));
        }

        actividades.sort(Comparator.comparing(ActividadPanelDto::getHora).reversed());

        if (actividades.size() > 10) {
            return actividades.subList(0, 10);
        }
        return actividades;
    }

    private String obtenerEstadoRutina(RutinaDTO rutina) {
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
