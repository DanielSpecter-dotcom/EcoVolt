package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.response.ConsumptionCompareResponseDto;
import com.ecovolt.demo.dtos.response.ConsumptionResponseDto;
import com.ecovolt.demo.dtos.response.RoomConsumptionResponseDto;

public interface IConsumoService {

    RoomConsumptionResponseDto obtenerConsumoHabitacion(Long habitacionId, Long usuarioId);

    ConsumptionCompareResponseDto compararConsumo(Long usuarioId);

    // Debe consolidar lecturas historicas y exponer consumo diario, semanal y mensual.
    ConsumptionResponseDto obtenerConsumoDispositivo(Long dispositivoId, Long usuarioId);
}
