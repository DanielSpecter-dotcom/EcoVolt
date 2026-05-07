package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.NotificationSettingsDto;
import com.ecovolt.demo.dtos.request.UpdatePasswordDto;
import com.ecovolt.demo.dtos.request.UpdateUserProfileDto;
import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.UsuarioResponseDto;
import com.ecovolt.demo.serviceimpl.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserProfileDto request) {
        UsuarioResponseDto data = usuarioService.updateProfile(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Perfil actualizado exitosamente", data));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePasswordDto request) {
        usuarioService.updatePassword(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Contrasena actualizada exitosamente", null));
    }

    @PatchMapping("/{id}/notification-settings")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> updateNotificationSettings(
            @PathVariable Long id,
            @Valid @RequestBody NotificationSettingsDto request) {
        UsuarioResponseDto data = usuarioService.updateNotificationSettings(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Configuracion de notificaciones actualizada", data));
    }
}
