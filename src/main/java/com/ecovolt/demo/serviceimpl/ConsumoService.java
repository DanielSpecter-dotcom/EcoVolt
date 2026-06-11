package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.ItemComparacionConsumoDto;
import com.ecovolt.demo.dtos.ComparacionConsumoRespuestaDto;
import com.ecovolt.demo.dtos.ConsumoRespuestaDto;
import com.ecovolt.demo.dtos.HistoricoDTO;
import com.ecovolt.demo.dtos.ConsumoHabitacionDTO;
import com.ecovolt.demo.entities.Habitacion;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.HabitacionRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsumoService {

    private final HabitacionRepositorio habitacionRepositorio;
    private final HistoricoRepositorio historicoRepositorio;
    private final DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    private final ModelMapper modelMapper;

    public ConsumoService(HabitacionRepositorio habitacionRepositorio,
                          HistoricoRepositorio historicoRepositorio,
                          DispositivoVirtualRepositorio dispositivoVirtualRepositorio,
                          ModelMapper modelMapper) {
        this.habitacionRepositorio = habitacionRepositorio;
        this.historicoRepositorio = historicoRepositorio;
        this.dispositivoVirtualRepositorio = dispositivoVirtualRepositorio;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public HistoricoDTO create(HistoricoDTO request) {
        DispositivoVirtual dispositivo = findDevice(request.getDispositivoId());
        Historico historico = modelMapper.map(request, Historico.class);

        historico.setId(null);
        historico.setDispositivo(dispositivo);
        historico = historicoRepositorio.save(historico);

        HistoricoDTO historicoDTO = modelMapper.map(historico, HistoricoDTO.class);
        historicoDTO.setDispositivoId(historico.getDispositivo().getId());
        historicoDTO.setDispositivoNombre(historico.getDispositivo().getNombre());
        return historicoDTO;
    }

    @Transactional(readOnly = true)
    public List<HistoricoDTO> findAll(Long usuarioId) {
        return historicoRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(usuarioId)
                .stream()
                .map(historico -> {
                    HistoricoDTO historicoDTO = modelMapper.map(historico, HistoricoDTO.class);
                    historicoDTO.setDispositivoId(historico.getDispositivo().getId());
                    historicoDTO.setDispositivoNombre(historico.getDispositivo().getNombre());
                    return historicoDTO;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public HistoricoDTO findById(Long id) {
        Historico historico = findHistory(id);
        HistoricoDTO historicoDTO = modelMapper.map(historico, HistoricoDTO.class);
        historicoDTO.setDispositivoId(historico.getDispositivo().getId());
        historicoDTO.setDispositivoNombre(historico.getDispositivo().getNombre());
        return historicoDTO;
    }

    @Transactional
    public HistoricoDTO update(Long id, HistoricoDTO request) {
        Historico historico = findHistory(id);
        DispositivoVirtual dispositivo = findDevice(request.getDispositivoId());

        modelMapper.map(request, historico);
        historico.setId(id);
        historico.setDispositivo(dispositivo);

        historico = historicoRepositorio.save(historico);
        HistoricoDTO historicoDTO = modelMapper.map(historico, HistoricoDTO.class);
        historicoDTO.setDispositivoId(historico.getDispositivo().getId());
        historicoDTO.setDispositivoNombre(historico.getDispositivo().getNombre());
        return historicoDTO;
    }

    @Transactional
    public void delete(Long id) {
        Historico historico = findHistory(id);
        historicoRepositorio.delete(historico);
    }

    @Transactional(readOnly = true)
    public ConsumoHabitacionDTO obtenerConsumoHabitacion(Long habitacionId, Long usuarioId) {
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

        return ConsumoHabitacionDTO.builder()
                .roomId(habitacion.getId())
                .roomName(habitacion.getNombre())
                .totalKwh(round(totalKwh))
                .totalDurationMinutes(totalDuration)
                .devices(dispositivos)
                .build();
    }

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

    private DispositivoVirtual findDevice(Long dispositivoId) {
        if (dispositivoId == null) {
            throw new ResourceNotFoundException("Dispositivo no encontrado");
        }

        return dispositivoVirtualRepositorio.findById(dispositivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));
    }

}
