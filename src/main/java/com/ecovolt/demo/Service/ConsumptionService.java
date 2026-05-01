package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Response.ConsumptionCompareItemDto;
import com.ecovolt.demo.Dto.Response.ConsumptionCompareResponseDto;
import com.ecovolt.demo.Dto.Response.RoomConsumptionResponseDto;
import com.ecovolt.demo.Entities.HabitacionEntity;
import com.ecovolt.demo.Entities.HistoricoEntity;
import com.ecovolt.demo.Entities.VirtualDeviceEntity;
import com.ecovolt.demo.Exception.ResourceNotFoundException;
import com.ecovolt.demo.Repository.HabitacionRepository;
import com.ecovolt.demo.Repository.HistoricoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private final HabitacionRepository habitacionRepository;
    private final HistoricoRepository historicoRepository;

    @Transactional(readOnly = true)
    public RoomConsumptionResponseDto getRoomConsumption(Long roomId, Long userId) {
        HabitacionEntity room = habitacionRepository.findByIdAndCasaUsuarioId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));

        List<HistoricoEntity> history = historicoRepository
                .findByDispositivoHabitacionIdAndDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(
                        roomId,
                        userId
                );

        List<ConsumptionCompareItemDto> devices = buildDeviceComparison(history);
        double totalKwh = devices.stream().mapToDouble(ConsumptionCompareItemDto::getTotalKwh).sum();
        int totalDuration = history.stream()
                .map(HistoricoEntity::getDuracionMinutos)
                .filter(minutes -> minutes != null)
                .mapToInt(Integer::intValue)
                .sum();

        return RoomConsumptionResponseDto.builder()
                .roomId(room.getId())
                .roomName(room.getNombre())
                .totalKwh(round(totalKwh))
                .totalDurationMinutes(totalDuration)
                .devices(devices)
                .build();
    }

    @Transactional(readOnly = true)
    public ConsumptionCompareResponseDto compareConsumption(Long userId) {
        List<HistoricoEntity> history = historicoRepository
                .findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(userId);
        List<ConsumptionCompareItemDto> devices = buildDeviceComparison(history);
        double totalKwh = devices.stream().mapToDouble(ConsumptionCompareItemDto::getTotalKwh).sum();

        return ConsumptionCompareResponseDto.builder()
                .totalKwh(round(totalKwh))
                .devices(devices)
                .build();
    }

    private List<ConsumptionCompareItemDto> buildDeviceComparison(List<HistoricoEntity> history) {
        Map<Long, VirtualDeviceEntity> devicesById = history.stream()
                .map(HistoricoEntity::getDispositivo)
                .collect(Collectors.toMap(
                        VirtualDeviceEntity::getId,
                        device -> device,
                        (current, ignored) -> current,
                        LinkedHashMap::new
                ));

        Map<Long, Double> totalsByDevice = history.stream()
                .collect(Collectors.groupingBy(
                        historyItem -> historyItem.getDispositivo().getId(),
                        LinkedHashMap::new,
                        Collectors.summingDouble(HistoricoEntity::getKwhConsumidos)
                ));

        double totalKwh = totalsByDevice.values().stream().mapToDouble(Double::doubleValue).sum();

        return totalsByDevice.entrySet()
                .stream()
                .map(entry -> buildDeviceItem(devicesById.get(entry.getKey()), entry.getValue(), totalKwh))
                .sorted(Comparator.comparing(ConsumptionCompareItemDto::getTotalKwh).reversed())
                .toList();
    }

    private ConsumptionCompareItemDto buildDeviceItem(VirtualDeviceEntity device, Double deviceTotal, double totalKwh) {
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

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
