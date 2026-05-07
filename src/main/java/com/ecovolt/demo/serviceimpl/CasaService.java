package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.ModoAusenteDto;
import com.ecovolt.demo.dtos.CasaDTO;
import com.ecovolt.demo.dtos.ModoAusenteRespuestaDto;
import com.ecovolt.demo.entities.Casa;
import com.ecovolt.demo.entities.Usuario;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.CasaRepositorio;
import com.ecovolt.demo.repositories.UsuarioRepositorio;
import com.ecovolt.demo.services.RoutineService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CasaService {

    private final RoutineService rutinaService;
    private final CasaRepositorio casaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ModelMapper modelMapper;

    public CasaService(RoutineService rutinaService,
                       CasaRepositorio casaRepositorio,
                       UsuarioRepositorio usuarioRepositorio,
                       ModelMapper modelMapper) {
        this.rutinaService = rutinaService;
        this.casaRepositorio = casaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.modelMapper = modelMapper;
    }

    public CasaDTO create(CasaDTO request) {
        Usuario usuario = usuarioRepositorio.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Casa casa = modelMapper.map(request, Casa.class);
        casa.setId(null);
        casa.setUsuario(usuario);
        casa = casaRepositorio.save(casa);

        CasaDTO casaDTO = modelMapper.map(casa, CasaDTO.class);
        casaDTO.setUsuarioId(casa.getUsuario().getId());
        return casaDTO;
    }

    @Transactional(readOnly = true)
    public List<CasaDTO> findAll() {
        return casaRepositorio.findAll()
                .stream()
                .map(casa -> {
                    CasaDTO casaDTO = modelMapper.map(casa, CasaDTO.class);
                    casaDTO.setUsuarioId(casa.getUsuario().getId());
                    return casaDTO;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CasaDTO findById(Long id) {
        Casa casa = findHome(id);
        CasaDTO casaDTO = modelMapper.map(casa, CasaDTO.class);
        casaDTO.setUsuarioId(casa.getUsuario().getId());
        return casaDTO;
    }

    public CasaDTO update(Long id, CasaDTO request) {
        Casa casa = findHome(id);
        Usuario usuario = casa.getUsuario();

        modelMapper.map(request, casa);
        casa.setId(id);
        casa.setUsuario(usuario);
        casa = casaRepositorio.save(casa);

        CasaDTO casaDTO = modelMapper.map(casa, CasaDTO.class);
        casaDTO.setUsuarioId(casa.getUsuario().getId());
        return casaDTO;
    }

    public void delete(Long id) {
        if (casaRepositorio.existsById(id)) {
            casaRepositorio.deleteById(id);
        }
    }

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
}
