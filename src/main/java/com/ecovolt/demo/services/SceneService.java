package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.CrearEscenaDto;
import com.ecovolt.demo.dtos.ActivacionEscenaDTO;
import com.ecovolt.demo.dtos.EscenaDTO;

import java.util.List;

public interface SceneService {

    // Debe persistir la escena y sus estados deseados por dispositivo.
    EscenaDTO create(CrearEscenaDto request);

    // Debe aplicar en bloque los estados configurados sobre los dispositivos de la escena.
    ActivacionEscenaDTO activate(Long sceneId);

    // Debe retornar las escenas visibles para el dashboard.
    List<EscenaDTO> findAll();

    EscenaDTO findById(Long sceneId);

    EscenaDTO update(Long sceneId, CrearEscenaDto request);

    void delete(Long sceneId);
}
