package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.ModoAusenteDto;
import com.ecovolt.demo.dtos.RespuestaApi;
import com.ecovolt.demo.dtos.CasaDTO;
import com.ecovolt.demo.dtos.ModoAusenteRespuestaDto;
import com.ecovolt.demo.serviceimpl.CasaService;
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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ecovolt.demo.security.CustomUserDetails;
import java.util.List;

@RestController
@RequestMapping("/api/v1/homes")
public class CasaController {

    @Autowired
    private CasaService casaService;

    @PostMapping("/insertarcasa")
    public ResponseEntity<RespuestaApi<CasaDTO>> create(
            @RequestBody CasaDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CasaDTO data = casaService.create(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Casa creada exitosamente", data));
    }

    @GetMapping("/listarcasas")
    public ResponseEntity<RespuestaApi<List<CasaDTO>>> findAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CasaDTO> data = casaService.findAll(userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Casas obtenidas exitosamente", data));
    }

    @GetMapping("/encontrarcasa/{id}")
    public ResponseEntity<RespuestaApi<CasaDTO>> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CasaDTO data = casaService.findById(id, userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Casa obtenida exitosamente", data));
    }

    @PutMapping("/actualizarcasa/{id}")
    public ResponseEntity<RespuestaApi<CasaDTO>> update(
            @PathVariable Long id,
            @RequestBody CasaDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CasaDTO data = casaService.update(id, request, userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Casa actualizada exitosamente", data));
    }

    @DeleteMapping("/eliminarcasa/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        casaService.delete(id, userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Casa eliminada exitosamente", null));
    }

    @PatchMapping("/{id}/away-mode")
    public ResponseEntity<RespuestaApi<ModoAusenteRespuestaDto>> updateAwayMode(
            @PathVariable Long id,
            @Valid @RequestBody ModoAusenteDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ModoAusenteRespuestaDto data = casaService.updateAwayMode(id, request, userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Modo ausente actualizado exitosamente", data));
    }
}
