package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.SceneCreateDto;
import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.SceneActivationResponseDto;
import com.ecovolt.demo.dtos.response.SceneResponseDto;
import com.ecovolt.demo.services.SceneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scenes")
public class EscenaController {

    @Autowired
    private SceneService escenaService;

    @PostMapping
    public ResponseEntity<ApiResponse<SceneResponseDto>> create(@Valid @RequestBody SceneCreateDto request) {
        SceneResponseDto data = escenaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Escena creada exitosamente", data));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<SceneActivationResponseDto>> activate(@PathVariable Long id) {
        SceneActivationResponseDto data = escenaService.activate(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Escena activada exitosamente", data));
    }
}
