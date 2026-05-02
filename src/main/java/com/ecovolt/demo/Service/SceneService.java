package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.SceneCreateDto;
import com.ecovolt.demo.Dto.Response.SceneActivationResponseDto;
import com.ecovolt.demo.Dto.Response.SceneResponseDto;

public interface SceneService {

    // Debe persistir la escena y sus estados deseados por dispositivo.
    SceneResponseDto create(SceneCreateDto request);

    // Debe aplicar en bloque los estados configurados sobre los dispositivos de la escena.
    SceneActivationResponseDto activate(Long sceneId);
}
