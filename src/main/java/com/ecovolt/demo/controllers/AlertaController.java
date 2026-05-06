package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.AlertLimitRequestDto;
import com.ecovolt.demo.dtos.response.AlertResponseDto;
import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.LimitResponseDto;
import com.ecovolt.demo.Security.CustomUserDetails;
import com.ecovolt.demo.serviceimpl.AlertaService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @PostMapping("/limits")
    public ResponseEntity<ApiResponse<LimitResponseDto>> crearLimite(
            @Valid @RequestBody AlertLimitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LimitResponseDto data = alertaService.crearLimite(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Limite de consumo configurado exitosamente", data));
    }

    @PutMapping("/limits/{deviceId}")
    public ResponseEntity<ApiResponse<LimitResponseDto>> updateLimit(
            @PathVariable Long deviceId,
            @Valid @RequestBody AlertLimitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LimitResponseDto data = alertaService.updateLimit(deviceId, request, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Limite de consumo actualizado exitosamente", data));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AlertResponseDto>>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlertResponseDto> data = alertaService.getHistory(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Historial de alertas obtenido exitosamente", data));
    }

    @PatchMapping("/{alertId}/read")
    public ResponseEntity<ApiResponse<AlertResponseDto>> markAsRead(
            @PathVariable Long alertId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AlertResponseDto data = alertaService.markAsRead(alertId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Alerta marcada como leida", data));
    }
}
