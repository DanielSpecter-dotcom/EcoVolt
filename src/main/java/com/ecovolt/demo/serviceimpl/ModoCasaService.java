package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.request.ModoAusenteDto;
import com.ecovolt.demo.dtos.response.CasaRespuestaDto;
import com.ecovolt.demo.dtos.response.ModoAusenteRespuestaDto;
import com.ecovolt.demo.entities.Casa;
import com.ecovolt.demo.entities.Usuario;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.CasaRepositorio;
import com.ecovolt.demo.repositories.UsuarioRepositorio;
import com.ecovolt.demo.services.HomeModeService;
import com.ecovolt.demo.services.RoutineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ModoCasaService implements HomeModeService {

    private final RoutineService rutinaService;
    private final CasaRepositorio casaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public ModoCasaService(RoutineService rutinaService,
                           CasaRepositorio casaRepositorio,
                           UsuarioRepositorio usuarioRepositorio) {
        this.rutinaService = rutinaService;
        this.casaRepositorio = casaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public CasaRespuestaDto create(Casa request) {
        Usuario usuario = usuarioRepositorio.findById(request.getUsuario().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        request.setId(null);
        request.setUsuario(usuario);
        return toHomeResponse(casaRepositorio.save(request));
    }

    @Transactional(readOnly = true)
    public List<CasaRespuestaDto> findAll() {
        return casaRepositorio.findAll()
                .stream()
                .map(this::toHomeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CasaRespuestaDto findById(Long id) {
        return toHomeResponse(findHome(id));
    }

    public CasaRespuestaDto update(Long id, Casa request) {
        Casa casa = findHome(id);
        casa.setNombre(request.getNombre());
        return toHomeResponse(casaRepositorio.save(casa));
    }

    public void delete(Long id) {
        Casa casa = findHome(id);
        casaRepositorio.delete(casa);
    }

    @Override
    public ModoAusenteRespuestaDto updateAwayMode(Long homeId, ModoAusenteDto request) {
        findHome(homeId);
        int pausedRoutines = rutinaService.applyAwayMode(homeId, request.getModoAusente());

        return ModoAusenteRespuestaDto.builder()
                .homeId(homeId)
                .modoAusente(request.getModoAusente())
                .rutinasPausadas(pausedRoutines)
                .build();
    }

    private Casa findHome(Long id) {
        return casaRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Casa no encontrada"));
    }

    private CasaRespuestaDto toHomeResponse(Casa casa) {
        return CasaRespuestaDto.builder()
                .id(casa.getId())
                .nombre(casa.getNombre())
                .usuarioId(casa.getUsuario().getId())
                .build();
    }
}
