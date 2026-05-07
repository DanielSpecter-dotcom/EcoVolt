package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.components.SimuladorEnergiaUtils;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimuladorConsumoService {

    private final DispositivoVirtualRepositorio dispositivoRepo;
    private final HistoricoRepositorio historicoRepo;

    @Scheduled(fixedRate = 30000) // 30 segundos
    @Transactional
    public void ejecutarSimulacion() {
        List<DispositivoVirtual> activos = dispositivoRepo.findAll().stream()
                .filter(d -> d.isActivo() && !d.isEliminado())
                .toList();

        for (DispositivoVirtual dispositivo : activos) {
            double kwh = SimuladorEnergiaUtils.calcularConsumoIntervalo(dispositivo.getPotenciaWatts(), 30);

            Historico registro = Historico.builder()
                    .fechaRegistro(LocalDateTime.now())
                    .kwhConsumidos(kwh)
                    .duracionMinutos(1)
                    .dispositivo(dispositivo)
                    .build();

            historicoRepo.save(registro);
        }
    }
}
