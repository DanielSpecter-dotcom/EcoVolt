package com.ecovolt.demo.components;

import com.ecovolt.demo.dtos.EstadoActualDispositivoDto;
import com.ecovolt.demo.dtos.RutinaDTO;
import com.ecovolt.demo.entities.Alerta;
import com.ecovolt.demo.repositories.AlertaRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import com.ecovolt.demo.services.RoutineService;
import com.ecovolt.demo.serviceimpl.DispositivoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class RutinaScheduler {

    private final RoutineService routineService;
    private final DispositivoService dispositivoService;
    private final AlertaRepositorio alertaRepositorio;
    private final DispositivoVirtualRepositorio dispositivoVirtualRepositorio;

    public RutinaScheduler(RoutineService routineService,
                           DispositivoService dispositivoService,
                           AlertaRepositorio alertaRepositorio,
                           DispositivoVirtualRepositorio dispositivoVirtualRepositorio) {
        this.routineService = routineService;
        this.dispositivoService = dispositivoService;
        this.alertaRepositorio = alertaRepositorio;
        this.dispositivoVirtualRepositorio = dispositivoVirtualRepositorio;
    }

    @Scheduled(cron = "0 * * * * *") // cada minuto en el segundo 0
    @Transactional
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
                    boolean encender = Boolean.TRUE.equals(accion.getTurnOn());
                    EstadoActualDispositivoDto estado = new EstadoActualDispositivoDto();
                    estado.setStatus(encender ? "ON" : "OFF");
                    try {
                        dispositivoService.updateStatus(accion.getDeviceId(), estado);
                        dispositivoVirtualRepositorio.findById(accion.getDeviceId()).ifPresent(dispositivo -> {
                            String accionTexto = encender ? "encendió" : "apagó";
                            alertaRepositorio.save(Alerta.builder()
                                    .tipo("INFO")
                                    .mensaje("La rutina '" + rutina.getName() + "' " + accionTexto
                                            + " el dispositivo " + dispositivo.getNombre())
                                    .fechaCreacion(LocalDateTime.now())
                                    .leido(false)
                                    .dispositivo(dispositivo)
                                    .build());
                        });
                    } catch (Exception e) {
                        System.err.println("Error ejecutando acción en dispositivo "
                                + accion.getDeviceId() + ": " + e.getMessage());
                    }
                });
            }
        }
    }
}