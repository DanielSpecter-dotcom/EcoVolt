package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.AlertLimitRequestDto;
import com.ecovolt.demo.dtos.response.AlertResponseDto;
import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.LimitResponseDto;
import com.ecovolt.demo.security.CustomUserDetails;
import com.ecovolt.demo.serviceimpl.AlertaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/limits")
    public ResponseEntity<ApiResponse<LimitResponseDto>> crearLimite(
            @Valid @RequestBody AlertLimitRequestDto solicitud,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LimitResponseDto respuesta = alertaService.crearLimite(solicitud, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Limite de consumo configurado exitosamente", respuesta));
    }

    @PutMapping("/limits/{dispositivoId}")
    public ResponseEntity<ApiResponse<LimitResponseDto>> actualizarLimite(
            @PathVariable Long dispositivoId,
            @Valid @RequestBody AlertLimitRequestDto solicitud,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LimitResponseDto respuesta = alertaService.actualizarLimite(dispositivoId, solicitud, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Limite de consumo actualizado exitosamente", respuesta));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AlertResponseDto>>> obtenerHistorial(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlertResponseDto> respuesta = alertaService.obtenerHistorial(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Historial de alertas obtenido exitosamente", respuesta));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<AlertResponseDto>>> filtrarAlertas(
            @RequestParam(name = "device", required = false) Long dispositivoId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlertResponseDto> respuesta = alertaService.filtrarAlertas(
                userDetails.getId(),
                dispositivoId,
                desde,
                hasta
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Alertas filtradas exitosamente", respuesta));
    }

    @PatchMapping("/{alertaId}/read")
    public ResponseEntity<ApiResponse<AlertResponseDto>> marcarComoLeida(
            @PathVariable Long alertaId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AlertResponseDto respuesta = alertaService.marcarComoLeida(alertaId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Alerta marcada como leida", respuesta));
    }
}
