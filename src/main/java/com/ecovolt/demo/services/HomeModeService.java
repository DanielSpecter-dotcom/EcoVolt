package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.request.ModoAusenteRequestDto;
import com.ecovolt.demo.dtos.response.ModoAusenteResponseDto;

public interface HomeModeService {

    // Debe coordinar el modo ausente de la casa con la pausa de rutinas automaticas.
    ModoAusenteResponseDto updateAwayMode(Long homeId, ModoAusenteRequestDto request);
}
