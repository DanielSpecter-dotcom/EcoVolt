package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.AwayModeRequestDto;
import com.ecovolt.demo.Dto.Response.AwayModeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultHomeModeService implements HomeModeService {

    private final RoutineService routineService;

    @Override
    public AwayModeResponseDto updateAwayMode(Long homeId, AwayModeRequestDto request) {
        int pausedRoutines = routineService.applyAwayMode(homeId, request.getAwayModeEnabled());

        return AwayModeResponseDto.builder()
                .homeId(homeId)
                .awayModeEnabled(request.getAwayModeEnabled())
                .pausedRoutines(pausedRoutines)
                .build();
    }
}
