package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Request.RoomCreateRequestDto;
import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.RoomResponseDto;
import com.ecovolt.demo.Service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Ambientes", description = "Gestion de ambientes del hogar")
public class RoomController {

    private final DeviceService deviceService;

    @Operation(summary = "Crear un nuevo ambiente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ambiente creado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponseDto>> create(@Valid @RequestBody RoomCreateRequestDto request) {
        RoomResponseDto data = deviceService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Ambiente creado exitosamente", data));
    }
}
