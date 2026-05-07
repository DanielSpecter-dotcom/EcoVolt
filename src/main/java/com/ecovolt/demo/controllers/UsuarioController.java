package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.ConfiguracionNotificacionesDto;
import com.ecovolt.demo.dtos.request.ActualizarContrasenaDto;
import com.ecovolt.demo.dtos.request.ActualizarPerfilUsuarioDto;
import com.ecovolt.demo.dtos.response.RespuestaApi;
import com.ecovolt.demo.dtos.response.UsuarioRespuestaDto;
import com.ecovolt.demo.entities.Rol;
import com.ecovolt.demo.entities.Usuario;
import com.ecovolt.demo.serviceimpl.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<RespuestaApi<UsuarioRespuestaDto>> create(@RequestBody Usuario request) {
        UsuarioRespuestaDto data = usuarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Usuario creado exitosamente", data));
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<UsuarioRespuestaDto>>> findAll() {
        List<UsuarioRespuestaDto> data = usuarioService.findAll();
        return ResponseEntity.ok(new RespuestaApi<>(true, "Usuarios obtenidos exitosamente", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<UsuarioRespuestaDto>> findById(@PathVariable Long id) {
        UsuarioRespuestaDto data = usuarioService.findById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Usuario obtenido exitosamente", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<UsuarioRespuestaDto>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPerfilUsuarioDto request) {
        UsuarioRespuestaDto data = usuarioService.updateProfile(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Perfil actualizado exitosamente", data));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<RespuestaApi<Void>> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarContrasenaDto request) {
        usuarioService.updatePassword(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Contrasena actualizada exitosamente", null));
    }

    @PatchMapping("/{id}/notification-settings")
    public ResponseEntity<RespuestaApi<UsuarioRespuestaDto>> updateNotificationSettings(
            @PathVariable Long id,
            @Valid @RequestBody ConfiguracionNotificacionesDto request) {
        UsuarioRespuestaDto data = usuarioService.updateNotificationSettings(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Configuracion de notificaciones actualizada", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Usuario eliminado exitosamente", null));
    }

    @PostMapping("/roles")
    public ResponseEntity<RespuestaApi<Rol>> createRole(@RequestBody Rol request) {
        Rol data = usuarioService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Rol creado exitosamente", data));
    }

    @GetMapping("/roles")
    public ResponseEntity<RespuestaApi<List<Rol>>> findAllRoles() {
        List<Rol> data = usuarioService.findAllRoles();
        return ResponseEntity.ok(new RespuestaApi<>(true, "Roles obtenidos exitosamente", data));
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<RespuestaApi<Rol>> findRoleById(@PathVariable Long id) {
        Rol data = usuarioService.findRoleById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Rol obtenido exitosamente", data));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<RespuestaApi<Rol>> updateRole(
            @PathVariable Long id,
            @RequestBody Rol request) {
        Rol data = usuarioService.updateRole(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Rol actualizado exitosamente", data));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<RespuestaApi<Void>> deleteRole(@PathVariable Long id) {
        usuarioService.deleteRole(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Rol eliminado exitosamente", null));
    }
}
