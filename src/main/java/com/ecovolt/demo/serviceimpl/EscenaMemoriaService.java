package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.request.EstadoDeseadoDispositivoDto;
import com.ecovolt.demo.dtos.request.CrearEscenaDto;
import com.ecovolt.demo.dtos.response.EstadoDispositivoRespuestaDto;
import com.ecovolt.demo.dtos.response.ActivacionEscenaRespuestaDto;
import com.ecovolt.demo.dtos.response.EscenaRespuestaDto;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.services.SceneService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class EscenaMemoriaService implements SceneService {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, EscenaRespuestaDto> scenes = new ConcurrentHashMap<>();

    @Override
    public EscenaRespuestaDto create(CrearEscenaDto request) {
        Long sceneId = sequence.getAndIncrement();
        EscenaRespuestaDto response = EscenaRespuestaDto.builder()
                .id(sceneId)
                .name(request.getNombre())
                .devices(mapDeviceStates(request.getDispostivos()))
                .build();

        scenes.put(sceneId, response);
        return response;
    }

    @Override
    public ActivacionEscenaRespuestaDto activate(Long sceneId) {
        EscenaRespuestaDto scene = scenes.get(sceneId);
        if (scene == null) {
            throw new ResourceNotFoundException("Escena no encontrada");
        }

        /*
         * La implementacion productiva debe validar permisos, cargar la escena
         * desde la base de datos y enviar cada comando al servicio de dispositivos
         * para cambiar su estado de forma transaccional o compensable.
         */
        return ActivacionEscenaRespuestaDto.builder()
                .sceneId(scene.getId())
                .activatedAt(LocalDateTime.now())
                .appliedDevices(scene.getDevices())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscenaRespuestaDto> findAll() {
        return scenes.values().stream()
                .sorted(java.util.Comparator.comparing(EscenaRespuestaDto::getId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EscenaRespuestaDto findById(Long sceneId) {
        return findScene(sceneId);
    }

    @Override
    public EscenaRespuestaDto update(Long sceneId, CrearEscenaDto request) {
        EscenaRespuestaDto scene = findScene(sceneId);
        scene.setName(request.getNombre());
        scene.setDevices(mapDeviceStates(request.getDispostivos()));
        scenes.put(sceneId, scene);
        return scene;
    }

    @Override
    public void delete(Long sceneId) {
        if (scenes.remove(sceneId) == null) {
            throw new ResourceNotFoundException("Escena no encontrada");
        }
    }

    private List<EstadoDispositivoRespuestaDto> mapDeviceStates(List<EstadoDeseadoDispositivoDto> devices) {
        return devices.stream()
                .map(device -> EstadoDispositivoRespuestaDto.builder()
                        .deviceId(device.getDeviceId())
                        .desiredOn(device.getDesiredOn())
                        .build())
                .toList();
    }

    private EscenaRespuestaDto findScene(Long sceneId) {
        EscenaRespuestaDto scene = scenes.get(sceneId);
        if (scene == null) {
            throw new ResourceNotFoundException("Escena no encontrada");
        }
        return scene;
    }
}
