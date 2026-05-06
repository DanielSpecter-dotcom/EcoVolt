package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.ConsumptionCompareResponseDto;
import com.ecovolt.demo.dtos.response.ConsumptionResponseDto;
import com.ecovolt.demo.dtos.response.RoomConsumptionResponseDto;
import com.ecovolt.demo.Security.CustomUserDetails;
import com.ecovolt.demo.services.IConsumoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consumption")
public class ConsumoController {

    @Autowired
    private IConsumoService consumoService;

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomConsumptionResponseDto>> obtenerConsumoHabitacion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        RoomConsumptionResponseDto data = consumoService.obtenerConsumoHabitacion(id, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Consumo de habitacion obtenido exitosamente", data));
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<ConsumptionCompareResponseDto>> compare(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConsumptionCompareResponseDto data = consumoService.compararConsumo(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Comparacion de consumo obtenida exitosamente", data));
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<ApiResponse<ConsumptionResponseDto>> obtenerConsumoDispositivo(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConsumptionResponseDto data = consumoService.obtenerConsumoDispositivo(id, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Consumo del dispositivo obtenido exitosamente", data));
    }
}
