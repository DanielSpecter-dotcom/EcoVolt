package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.AwayModeRequestDto;
import com.ecovolt.demo.Dto.Response.AwayModeResponseDto;

public interface HomeModeService {

    // Debe coordinar el modo ausente de la casa con la pausa de rutinas automaticas.
    AwayModeResponseDto updateAwayMode(Long homeId, AwayModeRequestDto request);
}
