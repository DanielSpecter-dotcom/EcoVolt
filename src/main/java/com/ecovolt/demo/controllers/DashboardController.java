package com.ecovolt.demo.controllers;

import com.ecovolt.demo.dtos.ActividadPanelDto;
import com.ecovolt.demo.dtos.RespuestaApi;
import com.ecovolt.demo.dtos.DispositivoPanelDto;
import com.ecovolt.demo.dtos.EscenasRutinasPanelDto;
import com.ecovolt.demo.dtos.ResumenPanelDto;
import com.ecovolt.demo.dtos.RutinaDTO;
import com.ecovolt.demo.dtos.ActivacionEscenaDTO;
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
    public ResponseEntity<RespuestaApi<ResumenPanelDto>> obtenerResumen(
            @AuthenticationPrincipal CustomUserDetails usuario) {
        ResumenPanelDto data = dashboardService.obtenerResumen(usuario.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Resumen del dashboard obtenido exitosamente", data));
    }

    @GetMapping("/devices")
    public ResponseEntity<RespuestaApi<List<DispositivoPanelDto>>> obtenerDispositivos(
            @AuthenticationPrincipal CustomUserDetails usuario) {
        List<DispositivoPanelDto> data = dashboardService.obtenerDispositivos(usuario.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Dispositivos del dashboard obtenidos exitosamente", data));
    }

    @GetMapping("/scenes-routines")
    public ResponseEntity<RespuestaApi<EscenasRutinasPanelDto>> obtenerEscenasRutinas(
            @AuthenticationPrincipal CustomUserDetails usuario) {
        EscenasRutinasPanelDto data = dashboardService.obtenerEscenasRutinas(usuario.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escenas y rutinas obtenidas exitosamente", data));
    }

    @PostMapping("/scenes/{id}/activate")
    public ResponseEntity<RespuestaApi<ActivacionEscenaDTO>> activarEscena(@PathVariable Long id) {
        ActivacionEscenaDTO data = dashboardService.activarEscena(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Escena activada exitosamente", data));
    }

    @PatchMapping("/routines/{id}/pause")
    public ResponseEntity<RespuestaApi<RutinaDTO>> pausarRutina(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails usuario) {
        RutinaDTO data = dashboardService.pausarRutina(id, usuario.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Rutina pausada exitosamente", data));
    }

    @GetMapping("/activity")
    public ResponseEntity<RespuestaApi<List<ActividadPanelDto>>> obtenerActividad(
            @AuthenticationPrincipal CustomUserDetails usuario) {
        List<ActividadPanelDto> data = dashboardService.obtenerActividad(usuario.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Actividad reciente obtenida exitosamente", data));
    }
}
