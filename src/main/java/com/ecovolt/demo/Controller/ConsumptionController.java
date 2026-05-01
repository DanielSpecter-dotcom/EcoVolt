package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.ConsumptionCompareResponseDto;
import com.ecovolt.demo.Dto.Response.RoomConsumptionResponseDto;
import com.ecovolt.demo.Security.CustomUserDetails;
import com.ecovolt.demo.Service.ConsumptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consumption")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomConsumptionResponseDto>> getRoomConsumption(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        RoomConsumptionResponseDto data = consumptionService.getRoomConsumption(id, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Consumo de habitacion obtenido exitosamente", data));
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<ConsumptionCompareResponseDto>> compare(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConsumptionCompareResponseDto data = consumptionService.compareConsumption(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Comparacion de consumo obtenida exitosamente", data));
    }
}
