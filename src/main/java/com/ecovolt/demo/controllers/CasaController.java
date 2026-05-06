package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.AwayModeRequestDto;
import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.AwayModeResponseDto;
import com.ecovolt.demo.services.HomeModeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/homes")
public class CasaController {

    @Autowired
    private HomeModeService modoCasaService;

    @PatchMapping("/{id}/away-mode")
    public ResponseEntity<ApiResponse<AwayModeResponseDto>> updateAwayMode(
            @PathVariable Long id,
            @Valid @RequestBody AwayModeRequestDto request) {
        AwayModeResponseDto data = modoCasaService.updateAwayMode(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Modo ausente actualizado exitosamente", data));
    }
}
