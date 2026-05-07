package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.request.ModoAusenteDto;
import com.ecovolt.demo.dtos.response.CasaRespuestaDto;
import com.ecovolt.demo.dtos.response.ModoAusenteRespuestaDto;
import com.ecovolt.demo.entities.Casa;

import java.util.List;

public interface HomeModeService {

    CasaRespuestaDto create(Casa request);

    List<CasaRespuestaDto> findAll();

    CasaRespuestaDto findById(Long id);

    CasaRespuestaDto update(Long id, Casa request);

    void delete(Long id);

    // Debe coordinar el modo ausente de la casa con la pausa de rutinas automaticas.
    ModoAusenteRespuestaDto updateAwayMode(Long homeId, ModoAusenteDto request);
}
