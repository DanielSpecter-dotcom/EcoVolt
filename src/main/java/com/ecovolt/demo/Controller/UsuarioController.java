package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Request.NotificationSettingsDto;
import com.ecovolt.demo.Dto.Request.UpdatePasswordDto;
import com.ecovolt.demo.Dto.Request.UpdateUserProfileDto;
import com.ecovolt.demo.Dto.Request.UsuarioCreateDto;
import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.UsuarioResponseDto;
import com.ecovolt.demo.Service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/EcoVolt/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> saveUsuario(@Valid @RequestBody UsuarioCreateDto usuarioCreateDto) {
        UsuarioResponseDto data = usuarioService.saveUsuario(usuarioCreateDto);
        return new ResponseEntity<>
                (new ApiResponse<>(true,"Usuario creado exitosamente", data), HttpStatus.CREATED);
    }

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
