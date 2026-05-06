package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.request.AwayModeRequestDto;
import com.ecovolt.demo.dtos.response.AwayModeResponseDto;

public interface HomeModeService {

    // Debe coordinar el modo ausente de la casa con la pausa de rutinas automaticas.
    AwayModeResponseDto updateAwayMode(Long homeId, AwayModeRequestDto request);
}
