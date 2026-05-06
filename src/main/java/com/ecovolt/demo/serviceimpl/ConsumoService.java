package com.ecovolt.demo.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.response.ConsumptionCompareItemDto;
import com.ecovolt.demo.dtos.response.ConsumptionCompareResponseDto;
import com.ecovolt.demo.dtos.response.ConsumptionResponseDto;
import com.ecovolt.demo.dtos.response.RoomConsumptionResponseDto;
import com.ecovolt.demo.entities.Habitacion;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.HabitacionRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import com.ecovolt.demo.services.IConsumoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsumoService implements IConsumoService {

    @Autowired
    private HabitacionRepositorio habitacionRepositorio;
    @Autowired
    private HistoricoRepositorio historicoRepositorio;
    @Autowired
    private DispositivoVirtualRepositorio dispositivoVirtualRepositorio;

    @Override
    @Transactional(readOnly = true)
    public RoomConsumptionResponseDto obtenerConsumoHabitacion(Long habitacionId, Long usuarioId) {
        Habitacion habitacion = habitacionRepositorio.findByIdAndCasaUsuarioId(habitacionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));

        List<Historico> historial = historicoRepositorio
                .findByDispositivoHabitacionIdAndDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(
                        habitacionId,
                        usuarioId
                );

        List<ConsumptionCompareItemDto> dispositivos = buildDeviceComparison(historial);
        double totalKwh = dispositivos.stream().mapToDouble(ConsumptionCompareItemDto::getTotalKwh).sum();
        int totalDuration = historial.stream()
                .map(Historico::getDuracionMinutos)
                .filter(minutes -> minutes != null)
                .mapToInt(Integer::intValue)
                .sum();

        return RoomConsumptionResponseDto.builder()
                .roomId(habitacion.getId())
                .roomName(habitacion.getNombre())
                .totalKwh(round(totalKwh))
                .totalDurationMinutes(totalDuration)
                .devices(dispositivos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumptionCompareResponseDto compararConsumo(Long usuarioId) {
        List<Historico> historial = historicoRepositorio
                .findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(usuarioId);
        List<ConsumptionCompareItemDto> dispositivos = buildDeviceComparison(historial);
        double totalKwh = dispositivos.stream().mapToDouble(ConsumptionCompareItemDto::getTotalKwh).sum();

        return ConsumptionCompareResponseDto.builder()
                .totalKwh(round(totalKwh))
                .devices(dispositivos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumptionResponseDto obtenerConsumoDispositivo(Long dispositivoId, Long usuarioId) {
        DispositivoVirtual device = dispositivoVirtualRepositorio.findByIdAndHabitacionCasaUsuarioId(dispositivoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));

        List<Historico> historial = historicoRepositorio
                .findByDispositivoIdAndDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(dispositivoId, usuarioId);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);

        /*
         * La capa de servicio consolida las lecturas automaticas del historial
         * energetico y devuelve ventanas de consumo listas para dashboards.
         */
        double dailyKwh = sumFrom(historial, today);
        double weeklyKwh = sumBetween(historial, weekStart, today);
        double monthlyKwh = sumBetween(historial, monthStart, today);

        return ConsumptionResponseDto.builder()
                .deviceId(device.getId())
                .deviceName(device.getNombre())
                .dailyKwh(round(dailyKwh))
                .weeklyKwh(round(weeklyKwh))
                .monthlyKwh(round(monthlyKwh))
                .build();
    }

    private List<ConsumptionCompareItemDto> buildDeviceComparison(List<Historico> historial) {
        Map<Long, DispositivoVirtual> dispositivosById = historial.stream()
                .map(Historico::getDispositivo)
                .collect(Collectors.toMap(
                        DispositivoVirtual::getId,
                        device -> device,
                        (current, ignored) -> current,
                        LinkedHashMap::new
                ));

        Map<Long, Double> totalsByDevice = historial.stream()
                .collect(Collectors.groupingBy(
                        historialItem -> historialItem.getDispositivo().getId(),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Historico::getKwhConsumidos)
                ));

        double totalKwh = totalsByDevice.values().stream().mapToDouble(Double::doubleValue).sum();

        return totalsByDevice.entrySet()
                .stream()
                .map(entry -> buildDeviceItem(dispositivosById.get(entry.getKey()), entry.getValue(), totalKwh))
                .sorted(Comparator.comparing(ConsumptionCompareItemDto::getTotalKwh).reversed())
                .toList();
    }

    private ConsumptionCompareItemDto buildDeviceItem(DispositivoVirtual device, Double deviceTotal, double totalKwh) {
        double percentage = totalKwh == 0 ? 0 : (deviceTotal / totalKwh) * 100;

        return ConsumptionCompareItemDto.builder()
                .deviceId(device.getId())
                .deviceName(device.getNombre())
                .roomId(device.getHabitacion().getId())
                .roomName(device.getHabitacion().getNombre())
                .totalKwh(round(deviceTotal))
                .percentage(round(percentage))
                .build();
    }

    private double sumFrom(List<Historico> historial, LocalDate date) {
        return historial.stream()
                .filter(item -> item.getFechaRegistro().toLocalDate().isEqual(date))
                .mapToDouble(Historico::getKwhConsumidos)
                .sum();
    }

    private double sumBetween(List<Historico> historial, LocalDate start, LocalDate end) {
        return historial.stream()
                .filter(item -> {
                    LocalDate itemDate = item.getFechaRegistro().toLocalDate();
                    return !itemDate.isBefore(start) && !itemDate.isAfter(end);
                })
                .mapToDouble(Historico::getKwhConsumidos)
                .sum();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
