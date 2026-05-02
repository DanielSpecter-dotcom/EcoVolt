package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Request.AwayModeRequestDto;
import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.AwayModeResponseDto;
import com.ecovolt.demo.Service.HomeModeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/homes")
@RequiredArgsConstructor
public class HomeController {

    private final HomeModeService homeModeService;

    @PatchMapping("/{id}/away-mode")
    public ResponseEntity<ApiResponse<AwayModeResponseDto>> updateAwayMode(
            @PathVariable Long id,
            @Valid @RequestBody AwayModeRequestDto request) {
        AwayModeResponseDto data = homeModeService.updateAwayMode(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Modo ausente actualizado exitosamente", data));
    }
}
