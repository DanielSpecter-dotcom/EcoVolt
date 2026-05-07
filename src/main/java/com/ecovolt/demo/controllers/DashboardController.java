package com.ecovolt.demo.controllers;

import com.ecovolt.demo.dtos.response.ActividadDashboardDto;
import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.DispositivoDashboardDto;
import com.ecovolt.demo.dtos.response.EscenasRutinasDashboardDto;
import com.ecovolt.demo.dtos.response.ResumenDashboardDto;
import com.ecovolt.demo.dtos.response.RoutineResponseDto;
import com.ecovolt.demo.dtos.response.SceneActivationResponseDto;
import com.ecovolt.demo.security.CustomUserDetails;
import com.ecovolt.demo.serviceimpl.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ResumenDashboardDto>> obtenerResumen(
            @AuthenticationPrincipal CustomUserDetails usuario) {
        ResumenDashboardDto data = dashboardService.obtenerResumen(usuario.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Resumen del dashboard obtenido exitosamente", data));
    }

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<DispositivoDashboardDto>>> obtenerDispositivos(
            @AuthenticationPrincipal CustomUserDetails usuario) {
        List<DispositivoDashboardDto> data = dashboardService.obtenerDispositivos(usuario.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Dispositivos del dashboard obtenidos exitosamente", data));
    }

    @GetMapping("/scenes-routines")
    public ResponseEntity<ApiResponse<EscenasRutinasDashboardDto>> obtenerEscenasRutinas() {
        EscenasRutinasDashboardDto data = dashboardService.obtenerEscenasRutinas();
        return ResponseEntity.ok(new ApiResponse<>(true, "Escenas y rutinas obtenidas exitosamente", data));
    }

    @PostMapping("/scenes/{id}/activate")
    public ResponseEntity<ApiResponse<SceneActivationResponseDto>> activarEscena(@PathVariable Long id) {
        SceneActivationResponseDto data = dashboardService.activarEscena(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Escena activada exitosamente", data));
    }

    @PatchMapping("/routines/{id}/pause")
    public ResponseEntity<ApiResponse<RoutineResponseDto>> pausarRutina(@PathVariable Long id) {
        RoutineResponseDto data = dashboardService.pausarRutina(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rutina pausada exitosamente", data));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<List<ActividadDashboardDto>>> obtenerActividad(
            @AuthenticationPrincipal CustomUserDetails usuario) {
        List<ActividadDashboardDto> data = dashboardService.obtenerActividad(usuario.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Actividad reciente obtenida exitosamente", data));
    }
}
