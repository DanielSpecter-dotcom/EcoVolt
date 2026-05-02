package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Request.SceneCreateDto;
import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.SceneActivationResponseDto;
import com.ecovolt.demo.Dto.Response.SceneResponseDto;
import com.ecovolt.demo.Service.SceneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scenes")
@RequiredArgsConstructor
public class SceneController {

    private final SceneService sceneService;

    @PostMapping
    public ResponseEntity<ApiResponse<SceneResponseDto>> create(@Valid @RequestBody SceneCreateDto request) {
        SceneResponseDto data = sceneService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Escena creada exitosamente", data));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<SceneActivationResponseDto>> activate(@PathVariable Long id) {
        SceneActivationResponseDto data = sceneService.activate(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Escena activada exitosamente", data));
    }
}
