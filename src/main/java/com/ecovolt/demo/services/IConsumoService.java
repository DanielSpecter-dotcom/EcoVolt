package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.response.ComparacionConsumoRespuestaDto;
import com.ecovolt.demo.dtos.response.ConsumoRespuestaDto;
import com.ecovolt.demo.dtos.response.HistoricoRespuestaDto;
import com.ecovolt.demo.dtos.response.ConsumoHabitacionRespuestaDto;
import com.ecovolt.demo.entities.Historico;

import java.util.List;

public interface IConsumoService {

    HistoricoRespuestaDto create(Historico request);

    List<HistoricoRespuestaDto> findAll();

    HistoricoRespuestaDto findById(Long id);

    HistoricoRespuestaDto update(Long id, Historico request);

    void delete(Long id);

    ConsumoHabitacionRespuestaDto obtenerConsumoHabitacion(Long habitacionId, Long usuarioId);

    ComparacionConsumoRespuestaDto compararConsumo(Long usuarioId);

    // Debe consolidar lecturas historicas y exponer consumo diario, semanal y mensual.
    ConsumoRespuestaDto obtenerConsumoDispositivo(Long dispositivoId, Long usuarioId);
}
