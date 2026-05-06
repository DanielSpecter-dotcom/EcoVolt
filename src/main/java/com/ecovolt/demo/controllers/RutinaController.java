package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.RoutineRequestDto;
import com.ecovolt.demo.dtos.request.RoutineUpdateRequestDto;
import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.RoutineResponseDto;
import com.ecovolt.demo.services.RoutineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routines")
public class RutinaController {

    @Autowired
    private RoutineService rutinaService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoutineResponseDto>> create(@Valid @RequestBody RoutineRequestDto request) {
        RoutineResponseDto data = rutinaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Rutina creada exitosamente", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoutineResponseDto>>> findAll() {
        List<RoutineResponseDto> data = rutinaService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Rutinas obtenidas exitosamente", data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RoutineResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody RoutineUpdateRequestDto request) {
        RoutineResponseDto data = rutinaService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rutina actualizada exitosamente", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        rutinaService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rutina eliminada exitosamente", null));
    }
}
