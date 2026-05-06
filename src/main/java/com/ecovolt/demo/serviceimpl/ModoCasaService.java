package com.ecovolt.demo.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.AwayModeRequestDto;
import com.ecovolt.demo.dtos.response.AwayModeResponseDto;
import com.ecovolt.demo.services.HomeModeService;
import com.ecovolt.demo.services.RoutineService;
import org.springframework.stereotype.Service;

@Service
public class ModoCasaService implements HomeModeService {

    @Autowired
    private RoutineService rutinaService;

    @Override
    public AwayModeResponseDto updateAwayMode(Long homeId, AwayModeRequestDto request) {
        int pausedRoutines = rutinaService.applyAwayMode(homeId, request.getAwayModeEnabled());

        return AwayModeResponseDto.builder()
                .homeId(homeId)
                .awayModeEnabled(request.getAwayModeEnabled())
                .pausedRoutines(pausedRoutines)
                .build();
    }
}
