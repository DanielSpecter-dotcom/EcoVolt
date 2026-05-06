package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.RoomCreateRequestDto;
import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.RoomResponseDto;
import com.ecovolt.demo.serviceimpl.DispositivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@Tag(name = "Ambientes", description = "Gestion de ambientes del hogar")
public class HabitacionController {

    @Autowired
    private DispositivoService dispositivoService;

    @Operation(summary = "Crear un nuevo ambiente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ambiente creado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponseDto>> create(@Valid @RequestBody RoomCreateRequestDto request) {
        RoomResponseDto data = dispositivoService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Ambiente creado exitosamente", data));
    }
}
