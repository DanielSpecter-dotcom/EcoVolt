package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.CrearEscenaDto;
import com.ecovolt.demo.dtos.RespuestaApi;
import com.ecovolt.demo.dtos.ActivacionEscenaDTO;
import com.ecovolt.demo.dtos.EscenaDTO;
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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ecovolt.demo.security.CustomUserDetails;
import java.util.List;

@RestController
@RequestMapping("/api/v1/scenes")
public class EscenaController {

    @Autowired
    private SceneService escenaService;

    @PostMapping
    public ResponseEntity<RespuestaApi<EscenaDTO>> create(@Valid @RequestBody CrearEscenaDto request) {
        EscenaDTO data = escenaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Escena creada exitosamente", data));
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<EscenaDTO>>> findAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<EscenaDTO> data = escenaService.findAll(userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escenas obtenidas exitosamente", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<EscenaDTO>> findById(@PathVariable Long id) {
        EscenaDTO data = escenaService.findById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escena obtenida exitosamente", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<EscenaDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody CrearEscenaDto request) {
        EscenaDTO data = escenaService.update(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escena actualizada exitosamente", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        escenaService.delete(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escena eliminada exitosamente", null));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<RespuestaApi<ActivacionEscenaDTO>> activate(@PathVariable Long id) {
        ActivacionEscenaDTO data = escenaService.activate(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escena activada exitosamente", data));
    }
}
