package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.EstadoDeseadoDispositivoDto;
import com.ecovolt.demo.dtos.CrearEscenaDto;
import com.ecovolt.demo.dtos.ActivacionEscenaDTO;
import com.ecovolt.demo.dtos.EscenaDTO;
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
    private final Map<Long, EscenaDTO> scenes = new ConcurrentHashMap<>();

    @Override
    public EscenaDTO create(CrearEscenaDto request) {
        Long sceneId = sequence.getAndIncrement();
        EscenaDTO response = EscenaDTO.builder()
                .id(sceneId)
                .name(request.getNombre())
                .devices(mapDeviceStates(request.getDispostivos()))
                .build();

        scenes.put(sceneId, response);
        return response;
    }

    @Override
    public ActivacionEscenaDTO activate(Long sceneId) {
        EscenaDTO scene = scenes.get(sceneId);
        if (scene == null) {
            throw new ResourceNotFoundException("Escena no encontrada");
        }

        /*
         * La implementacion productiva debe validar permisos, cargar la escena
         * desde la base de datos y enviar cada comando al servicio de dispositivos
         * para cambiar su estado de forma transaccional o compensable.
         */
        return ActivacionEscenaDTO.builder()
                .sceneId(scene.getId())
                .activatedAt(LocalDateTime.now())
                .appliedDevices(scene.getDevices())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscenaDTO> findAll() {
        return scenes.values().stream()
                .sorted(java.util.Comparator.comparing(EscenaDTO::getId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EscenaDTO findById(Long sceneId) {
        return findScene(sceneId);
    }

    @Override
    public EscenaDTO update(Long sceneId, CrearEscenaDto request) {
        EscenaDTO scene = findScene(sceneId);
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

    private List<EstadoDeseadoDispositivoDto> mapDeviceStates(List<EstadoDeseadoDispositivoDto> devices) {
        return devices.stream()
                .map(device -> EstadoDeseadoDispositivoDto.builder()
                        .deviceId(device.getDeviceId())
                        .desiredOn(device.getDesiredOn())
                        .build())
                .toList();
    }

    private EscenaDTO findScene(Long sceneId) {
        EscenaDTO scene = scenes.get(sceneId);
        if (scene == null) {
            throw new ResourceNotFoundException("Escena no encontrada");
        }
        return scene;
    }
}
