package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.ReportResponseDto;
import com.ecovolt.demo.Security.CustomUserDetails;
import com.ecovolt.demo.Service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ApiResponse<ReportResponseDto>> getReport(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReportResponseDto data = reportService.getReport(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Reporte de consumo obtenido exitosamente", data));
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@AuthenticationPrincipal CustomUserDetails userDetails) {
        byte[] pdf = reportService.exportPdf(userDetails.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ecovolt-report.pdf")
                .body(pdf);
    }
}
