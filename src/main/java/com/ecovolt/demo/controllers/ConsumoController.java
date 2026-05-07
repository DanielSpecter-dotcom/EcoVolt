package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.response.RespuestaApi;
import com.ecovolt.demo.dtos.response.ComparacionConsumoRespuestaDto;
import com.ecovolt.demo.dtos.response.ConsumoRespuestaDto;
import com.ecovolt.demo.dtos.response.HistoricoRespuestaDto;
import com.ecovolt.demo.dtos.response.ConsumoHabitacionRespuestaDto;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.security.CustomUserDetails;
import com.ecovolt.demo.services.IConsumoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1/consumption")
public class ConsumoController {

    @Autowired
    private IConsumoService consumoService;

    @PostMapping("/history")
    public ResponseEntity<RespuestaApi<HistoricoRespuestaDto>> create(@RequestBody Historico request) {
        HistoricoRespuestaDto data = consumoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Historico creado exitosamente", data));
    }

    @GetMapping("/history")
    public ResponseEntity<RespuestaApi<List<HistoricoRespuestaDto>>> findAll() {
        List<HistoricoRespuestaDto> data = consumoService.findAll();
        return ResponseEntity.ok(new RespuestaApi<>(true, "Historicos obtenidos exitosamente", data));
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<RespuestaApi<HistoricoRespuestaDto>> findById(@PathVariable Long id) {
        HistoricoRespuestaDto data = consumoService.findById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Historico obtenido exitosamente", data));
    }

    @PutMapping("/history/{id}")
    public ResponseEntity<RespuestaApi<HistoricoRespuestaDto>> update(
            @PathVariable Long id,
            @RequestBody Historico request) {
        HistoricoRespuestaDto data = consumoService.update(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Historico actualizado exitosamente", data));
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        consumoService.delete(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Historico eliminado exitosamente", null));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RespuestaApi<ConsumoHabitacionRespuestaDto>> obtenerConsumoHabitacion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConsumoHabitacionRespuestaDto data = consumoService.obtenerConsumoHabitacion(id, userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Consumo de habitacion obtenido exitosamente", data));
    }

    @GetMapping("/compare")
    public ResponseEntity<RespuestaApi<ComparacionConsumoRespuestaDto>> compare(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ComparacionConsumoRespuestaDto data = consumoService.compararConsumo(userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Comparacion de consumo obtenida exitosamente", data));
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<RespuestaApi<ConsumoRespuestaDto>> obtenerConsumoDispositivo(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConsumoRespuestaDto data = consumoService.obtenerConsumoDispositivo(id, userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Consumo del dispositivo obtenido exitosamente", data));
    }
}
