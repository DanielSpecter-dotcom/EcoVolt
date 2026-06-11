package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.CrearHabitacionDto;
import com.ecovolt.demo.dtos.HabitacionDTO;
import com.ecovolt.demo.entities.Casa;
import com.ecovolt.demo.entities.Habitacion;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.CasaRepositorio;
import com.ecovolt.demo.repositories.HabitacionRepositorio;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HabitacionService {

    private final CasaRepositorio casaRepositorio;
    private final HabitacionRepositorio habitacionRepositorio;
    private final ModelMapper modelMapper;

    public HabitacionService(CasaRepositorio casaRepositorio,
                             HabitacionRepositorio habitacionRepositorio,
                             ModelMapper modelMapper) {
        this.casaRepositorio = casaRepositorio;
        this.habitacionRepositorio = habitacionRepositorio;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public HabitacionDTO create(CrearHabitacionDto request) {
        Casa casa = casaRepositorio.findById(request.getCasaId())
                .orElseThrow(() -> new ResourceNotFoundException("Casa no encontrada"));

        Habitacion habitacion = modelMapper.map(request, Habitacion.class);
        habitacion.setId(null);
        habitacion.setNombre(request.getNombre().trim());
        habitacion.setCasa(casa);
        habitacion = habitacionRepositorio.save(habitacion);

        HabitacionDTO habitacionDTO = modelMapper.map(habitacion, HabitacionDTO.class);
        habitacionDTO.setName(habitacion.getNombre());
        habitacionDTO.setCasaId(habitacion.getCasa().getId());
        return habitacionDTO;
    }

    @Transactional(readOnly = true)
    public List<HabitacionDTO> findAll(Long usuarioId) {
        return habitacionRepositorio.findByCasaUsuarioIdOrderByIdAsc(usuarioId)
                .stream()
                .map(habitacion -> {
                    HabitacionDTO habitacionDTO = modelMapper.map(habitacion, HabitacionDTO.class);
                    habitacionDTO.setName(habitacion.getNombre());
                    habitacionDTO.setCasaId(habitacion.getCasa().getId());
                    return habitacionDTO;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitacionDTO findById(Long id) {
        Habitacion habitacion = habitacionRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));

        HabitacionDTO habitacionDTO = modelMapper.map(habitacion, HabitacionDTO.class);
        habitacionDTO.setName(habitacion.getNombre());
        habitacionDTO.setCasaId(habitacion.getCasa().getId());
        return habitacionDTO;
    }

    @Transactional
    public HabitacionDTO update(Long id, CrearHabitacionDto request) {
        Habitacion habitacion = habitacionRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));
        Casa casa = casaRepositorio.findById(request.getCasaId())
                .orElseThrow(() -> new ResourceNotFoundException("Casa no encontrada"));

        modelMapper.map(request, habitacion);
        habitacion.setNombre(request.getNombre().trim());
        habitacion.setCasa(casa);
        habitacion = habitacionRepositorio.save(habitacion);

        HabitacionDTO habitacionDTO = modelMapper.map(habitacion, HabitacionDTO.class);
        habitacionDTO.setName(habitacion.getNombre());
        habitacionDTO.setCasaId(habitacion.getCasa().getId());
        return habitacionDTO;
    }

    @Transactional
    public void delete(Long id) {
        if (!habitacionRepositorio.existsById(id)) {
            throw new ResourceNotFoundException("Habitacion no encontrada");
        }
        habitacionRepositorio.deleteById(id);
    }
}
