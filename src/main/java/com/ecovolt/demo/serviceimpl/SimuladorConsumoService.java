package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.components.SimuladorEnergiaUtils;
import com.ecovolt.demo.entities.Alerta;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.repositories.AlertaRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SimuladorConsumoService {

    private static final int INTERVALO_SEGUNDOS = 30;
    private static final int DURACION_MINIMA_MINUTOS = 1;
    private static final String CONSUMO_EXCESIVO = "CONSUMO_EXCESIVO";
    private static final String CONSUMO_ELEVADO = "CONSUMO_ELEVADO";
    private static final double WARNING_THRESHOLD = 0.75;

    private final DispositivoVirtualRepositorio dispositivoRepo;
    private final HistoricoRepositorio historicoRepo;
    private final AlertaRepositorio alertaRepo;

    @Scheduled(fixedRate = INTERVALO_SEGUNDOS * 1000L)
    @Transactional
    public void ejecutarSimulacion() {
        List<DispositivoVirtual> activos = dispositivoRepo.findAll().stream()
                .filter(d -> d.isActivo() && !d.isEliminado())
                .toList();

        for (DispositivoVirtual dispositivo : activos) {
            double kwhIntervalo = SimuladorEnergiaUtils.calcularConsumoIntervalo(
                    dispositivo.getPotenciaWatts(),
                    INTERVALO_SEGUNDOS
            );
            LocalDateTime ahora = LocalDateTime.now();

            Historico registro = obtenerRegistroDelDia(dispositivo, ahora.toLocalDate())
                    .map(historial -> acumularConsumo(historial, kwhIntervalo, ahora))
                    .orElseGet(() -> crearRegistro(dispositivo, kwhIntervalo, ahora));

            historicoRepo.save(registro);
            crearAlertaSiSuperaLimite(dispositivo);
        }
    }

    private Optional<Historico> obtenerRegistroDelDia(DispositivoVirtual dispositivo, LocalDate fecha) {
        return historicoRepo.findByDispositivoId(dispositivo.getId())
                .stream()
                .filter(historial -> historial.getFechaRegistro().toLocalDate().isEqual(fecha))
                .max(Comparator.comparing(Historico::getFechaRegistro));
    }

    private Historico acumularConsumo(Historico historial, double kwhIntervalo, LocalDateTime ahora) {
        double consumoActual = historial.getKwhConsumidos() == null ? 0 : historial.getKwhConsumidos();
        int duracionActual = historial.getDuracionMinutos() == null ? 0 : historial.getDuracionMinutos();

        historial.setFechaRegistro(ahora);
        historial.setKwhConsumidos(consumoActual + kwhIntervalo);
        historial.setDuracionMinutos(duracionActual + DURACION_MINIMA_MINUTOS);
        return historial;
    }

    private Historico crearRegistro(DispositivoVirtual dispositivo, double kwhIntervalo, LocalDateTime ahora) {
        return Historico.builder()
                .fechaRegistro(ahora)
                .kwhConsumidos(kwhIntervalo)
                .duracionMinutos(DURACION_MINIMA_MINUTOS)
                .dispositivo(dispositivo)
                .build();
    }

    private void crearAlertaSiSuperaLimite(DispositivoVirtual dispositivo) {
        if (dispositivo.getLimiteKwh() == null) return;

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate hoy = LocalDate.now();

        double consumoMensual = historicoRepo.findByDispositivoId(dispositivo.getId())
                .stream()
                .filter(h -> h.getKwhConsumidos() != null)
                .filter(h -> {
                    LocalDate fecha = h.getFechaRegistro().toLocalDate();
                    return !fecha.isBefore(inicioMes) && !fecha.isAfter(hoy);
                })
                .mapToDouble(Historico::getKwhConsumidos)
                .sum();

        double limite = dispositivo.getLimiteKwh();
        double ratio = consumoMensual / limite;

        LocalDateTime inicioPeriodo = inicioMes.atStartOfDay();
        LocalDateTime finPeriodo = LocalDateTime.now();

        if (ratio > 1.0) {
            if (!yaExisteAlertaEnPeriodo(dispositivo.getId(), CONSUMO_EXCESIVO, inicioPeriodo, finPeriodo)) {
                alertaRepo.save(Alerta.builder()
                        .tipo(CONSUMO_EXCESIVO)
                        .mensaje("El dispositivo " + dispositivo.getNombre()
                                + " superó el límite mensual de " + limite
                                + " kWh (consumo actual: " + String.format("%.1f", consumoMensual) + " kWh)")
                        .fechaCreacion(LocalDateTime.now())
                        .leido(false)
                        .dispositivo(dispositivo)
                        .build());
            }
        } else if (ratio >= WARNING_THRESHOLD) {
            if (!yaExisteAlertaEnPeriodo(dispositivo.getId(), CONSUMO_ELEVADO, inicioPeriodo, finPeriodo)) {
                alertaRepo.save(Alerta.builder()
                        .tipo(CONSUMO_ELEVADO)
                        .mensaje("El dispositivo " + dispositivo.getNombre()
                                + " alcanzó el " + String.format("%.0f", ratio * 100)
                                + "% del límite mensual ("
                                + String.format("%.1f", consumoMensual) + " / " + limite + " kWh)")
                        .fechaCreacion(LocalDateTime.now())
                        .leido(false)
                        .dispositivo(dispositivo)
                        .build());
            }
        }
    }

    /**
     * Evita recrear la misma alerta en cada ciclo de simulación: una vez generada para un
     * dispositivo+tipo dentro del periodo mensual vigente, no se vuelve a crear aunque el
     * usuario ya la haya marcado como leída (de lo contrario se duplicaría cada 30s mientras
     * el consumo siga por encima del umbral).
     */
    private boolean yaExisteAlertaEnPeriodo(Long dispositivoId, String tipo, LocalDateTime inicio, LocalDateTime fin) {
        return alertaRepo.existsByDispositivoIdAndTipoAndLeidoFalse(dispositivoId, tipo)
                || alertaRepo.existsByDispositivoIdAndTipoAndFechaCreacionBetween(dispositivoId, tipo, inicio, fin);
    }
}
