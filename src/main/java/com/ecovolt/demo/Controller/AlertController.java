package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Request.AlertLimitRequestDto;
import com.ecovolt.demo.Dto.Response.AlertResponseDto;
import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.LimitResponseDto;
import com.ecovolt.demo.Security.CustomUserDetails;
import com.ecovolt.demo.Service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/limits")
    public ResponseEntity<ApiResponse<LimitResponseDto>> createLimit(
            @Valid @RequestBody AlertLimitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LimitResponseDto data = alertService.createLimit(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Limite de consumo configurado exitosamente", data));
    }

    @PutMapping("/limits/{deviceId}")
    public ResponseEntity<ApiResponse<LimitResponseDto>> updateLimit(
            @PathVariable Long deviceId,
            @Valid @RequestBody AlertLimitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LimitResponseDto data = alertService.updateLimit(deviceId, request, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Limite de consumo actualizado exitosamente", data));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AlertResponseDto>>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlertResponseDto> data = alertService.getHistory(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Historial de alertas obtenido exitosamente", data));
    }

    @PatchMapping("/{alertId}/read")
    public ResponseEntity<ApiResponse<AlertResponseDto>> markAsRead(
            @PathVariable Long alertId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AlertResponseDto data = alertService.markAsRead(alertId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Alerta marcada como leida", data));
    }
}
