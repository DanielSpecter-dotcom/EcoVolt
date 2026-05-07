package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.LimiteAlertaSolicitudDto;
import com.ecovolt.demo.dtos.response.AlertaRespuestaDto;
import com.ecovolt.demo.dtos.response.RespuestaApi;
import com.ecovolt.demo.dtos.response.LimiteRespuestaDto;
import com.ecovolt.demo.entities.Alerta;
import com.ecovolt.demo.security.CustomUserDetails;
import com.ecovolt.demo.serviceimpl.AlertaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @PostMapping
    public ResponseEntity<RespuestaApi<AlertaRespuestaDto>> create(@RequestBody Alerta request) {
        AlertaRespuestaDto respuesta = alertaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Alerta creada exitosamente", respuesta));
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<AlertaRespuestaDto>>> findAll() {
        List<AlertaRespuestaDto> respuesta = alertaService.findAll();
        return ResponseEntity.ok(new RespuestaApi<>(true, "Alertas obtenidas exitosamente", respuesta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<AlertaRespuestaDto>> findById(@PathVariable Long id) {
        AlertaRespuestaDto respuesta = alertaService.findById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Alerta obtenida exitosamente", respuesta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<AlertaRespuestaDto>> update(
            @PathVariable Long id,
            @RequestBody Alerta request) {
        AlertaRespuestaDto respuesta = alertaService.update(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Alerta actualizada exitosamente", respuesta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        alertaService.delete(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Alerta eliminada exitosamente", null));
    }

    @PostMapping("/limits")
    public ResponseEntity<RespuestaApi<LimiteRespuestaDto>> crearLimite(
            @Valid @RequestBody LimiteAlertaSolicitudDto solicitud,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LimiteRespuestaDto respuesta = alertaService.crearLimite(solicitud, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Limite de consumo configurado exitosamente", respuesta));
    }

    @PutMapping("/limits/{dispositivoId}")
    public ResponseEntity<RespuestaApi<LimiteRespuestaDto>> actualizarLimite(
            @PathVariable Long dispositivoId,
            @Valid @RequestBody LimiteAlertaSolicitudDto solicitud,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LimiteRespuestaDto respuesta = alertaService.actualizarLimite(dispositivoId, solicitud, userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Limite de consumo actualizado exitosamente", respuesta));
    }

    @GetMapping("/history")
    public ResponseEntity<RespuestaApi<List<AlertaRespuestaDto>>> obtenerHistorial(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlertaRespuestaDto> respuesta = alertaService.obtenerHistorial(userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Historial de alertas obtenido exitosamente", respuesta));
    }

    @GetMapping("/filter")
    public ResponseEntity<RespuestaApi<List<AlertaRespuestaDto>>> filtrarAlertas(
            @RequestParam(name = "device", required = false) Long dispositivoId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlertaRespuestaDto> respuesta = alertaService.filtrarAlertas(
                userDetails.getId(),
                dispositivoId,
                desde,
                hasta
        );
        return ResponseEntity.ok(new RespuestaApi<>(true, "Alertas filtradas exitosamente", respuesta));
    }

    @PatchMapping("/{alertaId}/read")
    public ResponseEntity<RespuestaApi<AlertaRespuestaDto>> marcarComoLeida(
            @PathVariable Long alertaId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AlertaRespuestaDto respuesta = alertaService.marcarComoLeida(alertaId, userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Alerta marcada como leida", respuesta));
    }
}
