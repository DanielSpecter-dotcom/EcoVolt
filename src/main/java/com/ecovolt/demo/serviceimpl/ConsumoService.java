package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.response.ItemComparacionConsumoDto;
import com.ecovolt.demo.dtos.response.ComparacionConsumoRespuestaDto;
import com.ecovolt.demo.dtos.response.ConsumoRespuestaDto;
import com.ecovolt.demo.dtos.response.HistoricoRespuestaDto;
import com.ecovolt.demo.dtos.response.ConsumoHabitacionRespuestaDto;
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

    private final HabitacionRepositorio habitacionRepositorio;
    private final HistoricoRepositorio historicoRepositorio;
    private final DispositivoVirtualRepositorio dispositivoVirtualRepositorio;

    public ConsumoService(HabitacionRepositorio habitacionRepositorio,
                          HistoricoRepositorio historicoRepositorio,
                          DispositivoVirtualRepositorio dispositivoVirtualRepositorio) {
        this.habitacionRepositorio = habitacionRepositorio;
        this.historicoRepositorio = historicoRepositorio;
        this.dispositivoVirtualRepositorio = dispositivoVirtualRepositorio;
    }

    @Override
    @Transactional
    public HistoricoRespuestaDto create(Historico request) {
        DispositivoVirtual dispositivo = findDevice(request);

        request.setId(null);
        request.setDispositivo(dispositivo);
        return toHistoryResponse(historicoRepositorio.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoricoRespuestaDto> findAll() {
        return historicoRepositorio.findAll()
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoRespuestaDto findById(Long id) {
        return toHistoryResponse(findHistory(id));
    }

    @Override
    @Transactional
    public HistoricoRespuestaDto update(Long id, Historico request) {
        Historico historico = findHistory(id);
        DispositivoVirtual dispositivo = findDevice(request);

        historico.setFechaRegistro(request.getFechaRegistro());
        historico.setKwhConsumidos(request.getKwhConsumidos());
        historico.setDuracionMinutos(request.getDuracionMinutos());
        historico.setDispositivo(dispositivo);

        return toHistoryResponse(historicoRepositorio.save(historico));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Historico historico = findHistory(id);
        historicoRepositorio.delete(historico);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumoHabitacionRespuestaDto obtenerConsumoHabitacion(Long habitacionId, Long usuarioId) {
        Habitacion habitacion = habitacionRepositorio.findByIdAndCasaUsuarioId(habitacionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));

        List<Historico> historial = historicoRepositorio
                .findByDispositivoHabitacionIdAndDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(
                        habitacionId,
                        usuarioId
                );

        List<ItemComparacionConsumoDto> dispositivos = buildDeviceComparison(historial);
        double totalKwh = dispositivos.stream().mapToDouble(ItemComparacionConsumoDto::getTotalKwh).sum();
        int totalDuration = historial.stream()
                .map(Historico::getDuracionMinutos)
                .filter(minutes -> minutes != null)
                .mapToInt(Integer::intValue)
                .sum();

        return ConsumoHabitacionRespuestaDto.builder()
                .roomId(habitacion.getId())
                .roomName(habitacion.getNombre())
                .totalKwh(round(totalKwh))
                .totalDurationMinutes(totalDuration)
                .devices(dispositivos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ComparacionConsumoRespuestaDto compararConsumo(Long usuarioId) {
        List<Historico> historial = historicoRepositorio
                .findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(usuarioId);
        List<ItemComparacionConsumoDto> dispositivos = buildDeviceComparison(historial);
        double totalKwh = dispositivos.stream().mapToDouble(ItemComparacionConsumoDto::getTotalKwh).sum();

        return ComparacionConsumoRespuestaDto.builder()
                .totalKwh(round(totalKwh))
                .devices(dispositivos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumoRespuestaDto obtenerConsumoDispositivo(Long dispositivoId, Long usuarioId) {
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

        return ConsumoRespuestaDto.builder()
                .deviceId(device.getId())
                .deviceName(device.getNombre())
                .dailyKwh(round(dailyKwh))
                .weeklyKwh(round(weeklyKwh))
                .monthlyKwh(round(monthlyKwh))
                .build();
    }

    private List<ItemComparacionConsumoDto> buildDeviceComparison(List<Historico> historial) {
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
                .sorted(Comparator.comparing(ItemComparacionConsumoDto::getTotalKwh).reversed())
                .toList();
    }

    private ItemComparacionConsumoDto buildDeviceItem(DispositivoVirtual device, Double deviceTotal, double totalKwh) {
        double percentage = totalKwh == 0 ? 0 : (deviceTotal / totalKwh) * 100;

        return ItemComparacionConsumoDto.builder()
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

    private Historico findHistory(Long id) {
        return historicoRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historico no encontrado"));
    }

    private DispositivoVirtual findDevice(Historico historico) {
        if (historico.getDispositivo() == null || historico.getDispositivo().getId() == null) {
            throw new ResourceNotFoundException("Dispositivo no encontrado");
        }

        return dispositivoVirtualRepositorio.findById(historico.getDispositivo().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));
    }

    private HistoricoRespuestaDto toHistoryResponse(Historico historico) {
        DispositivoVirtual dispositivo = historico.getDispositivo();
        return HistoricoRespuestaDto.builder()
                .id(historico.getId())
                .fechaRegistro(historico.getFechaRegistro())
                .kwhConsumidos(historico.getKwhConsumidos())
                .duracionMinutos(historico.getDuracionMinutos())
                .dispositivoId(dispositivo.getId())
                .dispositivoNombre(dispositivo.getNombre())
                .build();
    }
}
