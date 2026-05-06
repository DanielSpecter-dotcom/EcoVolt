package com.ecovolt.demo.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.ModoAusenteRequestDto;
import com.ecovolt.demo.dtos.response.ModoAusenteResponseDto;
import com.ecovolt.demo.services.HomeModeService;
import com.ecovolt.demo.services.RoutineService;
import org.springframework.stereotype.Service;

@Service
public class ModoCasaService implements HomeModeService {

    @Autowired
    private RoutineService rutinaService;

    @Override
    public ModoAusenteResponseDto updateAwayMode(Long homeId, ModoAusenteRequestDto request) {
        int pausedRoutines = rutinaService.applyAwayMode(homeId, request.getModoAusente());

        return ModoAusenteResponseDto.builder()
                .homeId(homeId)
                .modoAusente(request.getModoAusente())
                .rutinasPausadas(pausedRoutines)
                .build();
    }
}
