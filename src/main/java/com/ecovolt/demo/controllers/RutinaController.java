package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.CrearRutinaDto;
import com.ecovolt.demo.dtos.request.ActualizarRutinaDto;
import com.ecovolt.demo.dtos.response.RespuestaApi;
import com.ecovolt.demo.dtos.response.RutinaRespuestaDto;
import com.ecovolt.demo.services.RoutineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routines")
public class RutinaController {

    @Autowired
    private RoutineService rutinaService;

    @PostMapping
    public ResponseEntity<RespuestaApi<RutinaRespuestaDto>> create(@Valid @RequestBody CrearRutinaDto request) {
        RutinaRespuestaDto data = rutinaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Rutina creada exitosamente", data));
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<RutinaRespuestaDto>>> findAll() {
        List<RutinaRespuestaDto> data = rutinaService.findAll();
        return ResponseEntity.ok(new RespuestaApi<>(true, "Rutinas obtenidas exitosamente", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<RutinaRespuestaDto>> findById(@PathVariable Long id) {
        RutinaRespuestaDto data = rutinaService.findById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Rutina obtenida exitosamente", data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RespuestaApi<RutinaRespuestaDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarRutinaDto request) {
        RutinaRespuestaDto data = rutinaService.update(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Rutina actualizada exitosamente", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        rutinaService.delete(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Rutina eliminada exitosamente", null));
    }
}
