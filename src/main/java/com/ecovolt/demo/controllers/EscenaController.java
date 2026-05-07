package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.CrearEscenaDto;
import com.ecovolt.demo.dtos.response.RespuestaApi;
import com.ecovolt.demo.dtos.response.ActivacionEscenaRespuestaDto;
import com.ecovolt.demo.dtos.response.EscenaRespuestaDto;
import com.ecovolt.demo.services.SceneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scenes")
public class EscenaController {

    @Autowired
    private SceneService escenaService;

    @PostMapping
    public ResponseEntity<RespuestaApi<EscenaRespuestaDto>> create(@Valid @RequestBody CrearEscenaDto request) {
        EscenaRespuestaDto data = escenaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Escena creada exitosamente", data));
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<EscenaRespuestaDto>>> findAll() {
        List<EscenaRespuestaDto> data = escenaService.findAll();
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escenas obtenidas exitosamente", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<EscenaRespuestaDto>> findById(@PathVariable Long id) {
        EscenaRespuestaDto data = escenaService.findById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escena obtenida exitosamente", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<EscenaRespuestaDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CrearEscenaDto request) {
        EscenaRespuestaDto data = escenaService.update(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escena actualizada exitosamente", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        escenaService.delete(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escena eliminada exitosamente", null));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<RespuestaApi<ActivacionEscenaRespuestaDto>> activate(@PathVariable Long id) {
        ActivacionEscenaRespuestaDto data = escenaService.activate(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escena activada exitosamente", data));
    }
}
