package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.Enums.TipoUsuario;
import com.ecovolt.demo.dtos.ModoAusenteDto;
import com.ecovolt.demo.dtos.CasaDTO;
import com.ecovolt.demo.dtos.ModoAusenteRespuestaDto;
import com.ecovolt.demo.entities.Casa;
import com.ecovolt.demo.entities.Usuario;
import com.ecovolt.demo.exceptions.BadRequestException;
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

    public CasaDTO create(CasaDTO request, Long usuarioId) {
        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getTipoUsuario() == TipoUsuario.PERSONAL
                && casaRepositorio.countByUsuarioId(usuarioId) >= 1) {
            throw new BadRequestException(
                    "Tu plan Personal permite gestionar 1 sola casa. Actualiza a EcoVolt Empresarial para administrar mas propiedades.");
        }

        Casa casa = modelMapper.map(request, Casa.class);
        casa.setId(null);
        casa.setUsuario(usuario);
        casa = casaRepositorio.save(casa);

        CasaDTO casaDTO = modelMapper.map(casa, CasaDTO.class);
        casaDTO.setUsuarioId(casa.getUsuario().getId());
        return casaDTO;
    }

    @Transactional(readOnly = true)
    public List<CasaDTO> findAll(Long usuarioId) {
        return casaRepositorio.findByUsuarioIdOrderByIdAsc(usuarioId)
                .stream()
                .map(casa -> {
                    CasaDTO casaDTO = modelMapper.map(casa, CasaDTO.class);
                    casaDTO.setUsuarioId(casa.getUsuario().getId());
                    return casaDTO;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CasaDTO findById(Long id, Long usuarioId) {
        Casa casa = findHome(id, usuarioId);
        CasaDTO casaDTO = modelMapper.map(casa, CasaDTO.class);
        casaDTO.setUsuarioId(casa.getUsuario().getId());
        return casaDTO;
    }

    public CasaDTO update(Long id, CasaDTO request, Long usuarioId) {
        Casa casa = findHome(id, usuarioId);
        Usuario usuario = casa.getUsuario();

        modelMapper.map(request, casa);
        casa.setId(id);
        casa.setUsuario(usuario);
        casa = casaRepositorio.save(casa);

        CasaDTO casaDTO = modelMapper.map(casa, CasaDTO.class);
        casaDTO.setUsuarioId(casa.getUsuario().getId());
        return casaDTO;
    }

    public void delete(Long id, Long usuarioId) {
        Casa casa = findHome(id, usuarioId);
        casaRepositorio.delete(casa);
    }

    public ModoAusenteRespuestaDto updateAwayMode(Long homeId, ModoAusenteDto request, Long usuarioId) {
        findHome(homeId, usuarioId);
        int pausedRoutines = rutinaService.applyAwayMode(homeId, request.getModoAusente());

        return ModoAusenteRespuestaDto.builder()
                .homeId(homeId)
                .modoAusente(request.getModoAusente())
                .rutinasPausadas(pausedRoutines)
                .build();
    }

    private Casa findHome(Long id, Long usuarioId) {
        Casa casa = casaRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Casa no encontrada"));
        if (!casa.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Casa no encontrada");
        }
        return casa;
    }
}
