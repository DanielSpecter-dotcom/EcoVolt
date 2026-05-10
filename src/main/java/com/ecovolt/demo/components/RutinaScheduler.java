package com.ecovolt.demo.components;

import com.ecovolt.demo.dtos.EstadoActualDispositivoDto;
import com.ecovolt.demo.dtos.RutinaDTO;
import com.ecovolt.demo.services.RoutineService;
import com.ecovolt.demo.serviceimpl.DispositivoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class RutinaScheduler {

    private final RoutineService routineService;
    private final DispositivoService dispositivoService;

    public RutinaScheduler(RoutineService routineService, DispositivoService dispositivoService) {
        this.routineService = routineService;
        this.dispositivoService = dispositivoService;
    }

    @Scheduled(cron = "0 * * * * *") // cada minuto en el segundo 0
    public void ejecutarRutinas() {
        ZoneId lima = ZoneId.of("America/Lima");
        LocalTime ahora = LocalTime.now(lima).truncatedTo(ChronoUnit.MINUTES);
        DayOfWeek hoy = LocalDate.now(lima).getDayOfWeek();

        List<RutinaDTO> rutinas = routineService.findAll();

        for (RutinaDTO rutina : rutinas) {
            boolean debeEjecutarse = Boolean.TRUE.equals(rutina.getEnabled())
                    && !Boolean.TRUE.equals(rutina.getPausedByAwayMode())
                    && rutina.getDaysOfWeek() != null
                    && rutina.getDaysOfWeek().contains(hoy)
                    && rutina.getExecutionTime() != null
                    && rutina.getExecutionTime().truncatedTo(ChronoUnit.MINUTES).equals(ahora);

            if (debeEjecutarse && rutina.getActions() != null) {
                rutina.getActions().forEach(accion -> {
                    EstadoActualDispositivoDto estado = new EstadoActualDispositivoDto();
                    estado.setStatus(Boolean.TRUE.equals(accion.getTurnOn()) ? "ON" : "OFF");
                    try {
                        dispositivoService.updateStatus(accion.getDeviceId(), estado);
                    } catch (Exception e) {
                        // log el error pero no romper el loop para las demás acciones
                        System.err.println("Error ejecutando acción en dispositivo "
                                + accion.getDeviceId() + ": " + e.getMessage());
                    }
                });
            }
        }
    }
}