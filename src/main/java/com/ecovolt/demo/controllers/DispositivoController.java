package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.CrearDispositivoDto;
import com.ecovolt.demo.dtos.ModoDispositivoDto;
import com.ecovolt.demo.dtos.AsignarHabitacionDispositivoDto;
import com.ecovolt.demo.dtos.EstadoActualDispositivoDto;
import com.ecovolt.demo.dtos.ActualizarDispositivoDto;
import com.ecovolt.demo.dtos.RespuestaApi;
import com.ecovolt.demo.dtos.DispositivoDTO;
import com.ecovolt.demo.serviceimpl.DispositivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ecovolt.demo.security.CustomUserDetails;
import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Dispositivos", description = "Gestion de dispositivos del hogar")
public class DispositivoController {

    @Autowired
    private DispositivoService dispositivoService;

    @Operation(summary = "Registrar un dispositivo virtual")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Dispositivo creado")
    @PostMapping({"", "/insertar"})
    public ResponseEntity<RespuestaApi<DispositivoDTO>> create(@Valid @RequestBody CrearDispositivoDto request) {
        DispositivoDTO data = dispositivoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Dispositivo virtual registrado exitosamente", data));
    }

    @Operation(summary = "Listar dispositivos", description = "Lista todos los dispositivos no eliminados con su estado actual")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dispositivos obtenidos")
    @GetMapping({"", "/listar"})
    public ResponseEntity<RespuestaApi<List<DispositivoDTO>>> findAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<DispositivoDTO> data = dispositivoService.findAll(userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Dispositivos obtenidos exitosamente", data));
    }

    @Operation(summary = "Obtener detalle de un dispositivo")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dispositivo obtenido")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<DispositivoDTO>> findById(@PathVariable Long id) {
        DispositivoDTO data = dispositivoService.findById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Dispositivo obtenido exitosamente", data));
    }

    @Operation(summary = "Asignar dispositivo a un ambiente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ambiente asignado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo o ambiente no encontrado")
    @PatchMapping("/{id}/room")
    public ResponseEntity<RespuestaApi<DispositivoDTO>> assignRoom(
            @PathVariable Long id,
            @Valid @RequestBody AsignarHabitacionDispositivoDto request) {
        DispositivoDTO data = dispositivoService.assignRoom(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Ambiente asignado exitosamente", data));
    }

    @Operation(summary = "Actualizar configuracion de un dispositivo")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dispositivo actualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo o ambiente no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<DispositivoDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarDispositivoDto request) {
        DispositivoDTO data = dispositivoService.update(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Dispositivo actualizado exitosamente", data));
    }

    @Operation(summary = "Eliminar dispositivo", description = "Realiza eliminacion logica para conservar los historicos en reportes")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dispositivo eliminado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo no encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        dispositivoService.delete(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Dispositivo eliminado exitosamente", null));
    }

    @Operation(summary = "Cambiar estado de un dispositivo", description = "Permite alternar entre ON y OFF")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado actualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo no encontrado")
    @PatchMapping("/{id}/status")
    public ResponseEntity<RespuestaApi<DispositivoDTO>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EstadoActualDispositivoDto request) {
        DispositivoDTO data = dispositivoService.updateStatus(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Estado del dispositivo actualizado exitosamente", data));
    }

    @Operation(summary = "Cambiar modo de funcionamiento", description = "Permite alternar entre AUTOMATIC y MANUAL")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Modo actualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo no encontrado")
    @PatchMapping("/{id}/mode")
    public ResponseEntity<RespuestaApi<DispositivoDTO>> updateMode(
            @PathVariable Long id,
            @Valid @RequestBody ModoDispositivoDto request) {
        DispositivoDTO data = dispositivoService.updateMode(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Modo del dispositivo actualizado exitosamente", data));
    }
}
