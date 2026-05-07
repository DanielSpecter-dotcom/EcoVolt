package com.ecovolt.demo.services;

import com.ecovolt.demo.dtos.ModoAusenteDto;
import com.ecovolt.demo.dtos.CasaDTO;
import com.ecovolt.demo.dtos.ModoAusenteRespuestaDto;
import com.ecovolt.demo.entities.Casa;

import java.util.List;

public interface HomeModeService {

    CasaDTO create(Casa request);

    List<CasaDTO> findAll();

    CasaDTO findById(Long id);

    CasaDTO update(Long id, Casa request);

    void delete(Long id);

    // Debe coordinar el modo ausente de la casa con la pausa de rutinas automaticas.
    ModoAusenteRespuestaDto updateAwayMode(Long homeId, ModoAusenteDto request);
}
