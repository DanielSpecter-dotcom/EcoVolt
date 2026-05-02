package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.DeviceStateRequestDto;
import com.ecovolt.demo.Dto.Request.SceneCreateDto;
import com.ecovolt.demo.Dto.Response.DeviceStateResponseDto;
import com.ecovolt.demo.Dto.Response.SceneActivationResponseDto;
import com.ecovolt.demo.Dto.Response.SceneResponseDto;
import com.ecovolt.demo.Exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemorySceneService implements SceneService {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, SceneResponseDto> scenes = new ConcurrentHashMap<>();

    @Override
    public SceneResponseDto create(SceneCreateDto request) {
        Long sceneId = sequence.getAndIncrement();
        SceneResponseDto response = SceneResponseDto.builder()
                .id(sceneId)
                .name(request.getName())
                .devices(mapDeviceStates(request.getDevices()))
                .build();

        scenes.put(sceneId, response);
        return response;
    }

    @Override
    public SceneActivationResponseDto activate(Long sceneId) {
        SceneResponseDto scene = scenes.get(sceneId);
        if (scene == null) {
            throw new ResourceNotFoundException("Escena no encontrada");
        }

        /*
         * La implementacion productiva debe validar permisos, cargar la escena
         * desde la base de datos y enviar cada comando al servicio de dispositivos
         * para cambiar su estado de forma transaccional o compensable.
         */
        return SceneActivationResponseDto.builder()
                .sceneId(scene.getId())
                .activatedAt(LocalDateTime.now())
                .appliedDevices(scene.getDevices())
                .build();
    }

    private List<DeviceStateResponseDto> mapDeviceStates(List<DeviceStateRequestDto> devices) {
        return devices.stream()
                .map(device -> DeviceStateResponseDto.builder()
                        .deviceId(device.getDeviceId())
                        .desiredOn(device.getDesiredOn())
                        .build())
                .toList();
    }
}
