package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Response.ConsumptionCompareResponseDto;
import com.ecovolt.demo.Dto.Response.ConsumptionResponseDto;
import com.ecovolt.demo.Dto.Response.RoomConsumptionResponseDto;

public interface IConsumptionService {

    RoomConsumptionResponseDto getRoomConsumption(Long roomId, Long userId);

    ConsumptionCompareResponseDto compareConsumption(Long userId);

    // Debe consolidar lecturas historicas y exponer consumo diario, semanal y mensual.
    ConsumptionResponseDto getDeviceConsumption(Long deviceId, Long userId);
}
