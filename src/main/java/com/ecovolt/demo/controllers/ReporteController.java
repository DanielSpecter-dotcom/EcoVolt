package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.RespuestaApi;
import com.ecovolt.demo.dtos.ReporteRespuestaDto;
import com.ecovolt.demo.security.CustomUserDetails;
import com.ecovolt.demo.serviceimpl.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping
    public ResponseEntity<RespuestaApi<ReporteRespuestaDto>> getReport(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReporteRespuestaDto data = reporteService.getReport(userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Reporte de consumo obtenido exitosamente", data));
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@AuthenticationPrincipal CustomUserDetails userDetails) {
        byte[] pdf = reporteService.exportPdf(userDetails.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ecovolt-report.pdf")
                .body(pdf);
    }
}
