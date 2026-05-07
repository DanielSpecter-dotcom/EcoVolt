package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.ModoAusenteDto;
import com.ecovolt.demo.dtos.response.RespuestaApi;
import com.ecovolt.demo.dtos.response.CasaRespuestaDto;
import com.ecovolt.demo.dtos.response.ModoAusenteRespuestaDto;
import com.ecovolt.demo.entities.Casa;
import com.ecovolt.demo.services.HomeModeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/homes")
public class CasaController {

    @Autowired
    private HomeModeService modoCasaService;

    @PostMapping
    public ResponseEntity<RespuestaApi<CasaRespuestaDto>> create(@RequestBody Casa request) {
        CasaRespuestaDto data = modoCasaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Casa creada exitosamente", data));
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<CasaRespuestaDto>>> findAll() {
        List<CasaRespuestaDto> data = modoCasaService.findAll();
        return ResponseEntity.ok(new RespuestaApi<>(true, "Casas obtenidas exitosamente", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<CasaRespuestaDto>> findById(@PathVariable Long id) {
        CasaRespuestaDto data = modoCasaService.findById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Casa obtenida exitosamente", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<CasaRespuestaDto>> update(
            @PathVariable Long id,
            @RequestBody Casa request) {
        CasaRespuestaDto data = modoCasaService.update(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Casa actualizada exitosamente", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        modoCasaService.delete(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Casa eliminada exitosamente", null));
    }

    @PatchMapping("/{id}/away-mode")
    public ResponseEntity<RespuestaApi<ModoAusenteRespuestaDto>> updateAwayMode(
            @PathVariable Long id,
            @Valid @RequestBody ModoAusenteDto request) {
        ModoAusenteRespuestaDto data = modoCasaService.updateAwayMode(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Modo ausente actualizado exitosamente", data));
    }
}
