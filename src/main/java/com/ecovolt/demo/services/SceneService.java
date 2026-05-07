package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.request.SceneCreateDto;
import com.ecovolt.demo.dtos.response.SceneActivationResponseDto;
import com.ecovolt.demo.dtos.response.SceneResponseDto;

import java.util.List;

public interface SceneService {

    // Debe persistir la escena y sus estados deseados por dispositivo.
    SceneResponseDto create(SceneCreateDto request);

    // Debe aplicar en bloque los estados configurados sobre los dispositivos de la escena.
    SceneActivationResponseDto activate(Long sceneId);

    // Debe retornar las escenas visibles para el dashboard.
    List<SceneResponseDto> findAll();
}
