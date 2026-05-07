package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.ComparacionConsumoRespuestaDto;
import com.ecovolt.demo.dtos.ConsumoRespuestaDto;
import com.ecovolt.demo.dtos.HistoricoDTO;
import com.ecovolt.demo.dtos.ConsumoHabitacionDTO;
import com.ecovolt.demo.entities.Historico;

import java.util.List;

public interface IConsumoService {

    HistoricoDTO create(Historico request);

    List<HistoricoDTO> findAll();

    HistoricoDTO findById(Long id);

    HistoricoDTO update(Long id, Historico request);

    void delete(Long id);

    ConsumoHabitacionDTO obtenerConsumoHabitacion(Long habitacionId, Long usuarioId);

    ComparacionConsumoRespuestaDto compararConsumo(Long usuarioId);

    // Debe consolidar lecturas historicas y exponer consumo diario, semanal y mensual.
    ConsumoRespuestaDto obtenerConsumoDispositivo(Long dispositivoId, Long usuarioId);
}
