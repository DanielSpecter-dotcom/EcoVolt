package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.request.CrearEscenaDto;
import com.ecovolt.demo.dtos.response.ActivacionEscenaRespuestaDto;
import com.ecovolt.demo.dtos.response.EscenaRespuestaDto;

import java.util.List;

public interface SceneService {

    // Debe persistir la escena y sus estados deseados por dispositivo.
    EscenaRespuestaDto create(CrearEscenaDto request);

    // Debe aplicar en bloque los estados configurados sobre los dispositivos de la escena.
    ActivacionEscenaRespuestaDto activate(Long sceneId);

    // Debe retornar las escenas visibles para el dashboard.
    List<EscenaRespuestaDto> findAll();

    EscenaRespuestaDto findById(Long sceneId);

    EscenaRespuestaDto update(Long sceneId, CrearEscenaDto request);

    void delete(Long sceneId);
}
