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
        if (dispositivo.getLimiteKwh() == null) {
            return;
        }

        double consumoAcumulado = historicoRepo.findByDispositivoId(dispositivo.getId())
                .stream()
                .map(Historico::getKwhConsumidos)
                .filter(kwh -> kwh != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        if (consumoAcumulado <= dispositivo.getLimiteKwh()
                || alertaRepo.existsByDispositivoIdAndTipoAndLeidoFalse(dispositivo.getId(), CONSUMO_EXCESIVO)) {
            return;
        }

        alertaRepo.save(Alerta.builder()
                .tipo(CONSUMO_EXCESIVO)
                .mensaje("El dispositivo " + dispositivo.getNombre() + " supero el limite de consumo configurado")
                .fechaCreacion(LocalDateTime.now())
                .leido(false)
                .dispositivo(dispositivo)
                .build());
    }
}
