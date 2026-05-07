package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.CrearHabitacionDto;
import com.ecovolt.demo.dtos.response.RespuestaApi;
import com.ecovolt.demo.dtos.response.HabitacionRespuestaDto;
import com.ecovolt.demo.serviceimpl.DispositivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<RespuestaApi<HabitacionRespuestaDto>> create(@Valid @RequestBody CrearHabitacionDto request) {
        HabitacionRespuestaDto data = dispositivoService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Ambiente creado exitosamente", data));
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<HabitacionRespuestaDto>>> findAll() {
        List<HabitacionRespuestaDto> data = dispositivoService.findAllRooms();
        return ResponseEntity.ok(new RespuestaApi<>(true, "Ambientes obtenidos exitosamente", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<HabitacionRespuestaDto>> findById(@PathVariable Long id) {
        HabitacionRespuestaDto data = dispositivoService.findRoomById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Ambiente obtenido exitosamente", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<HabitacionRespuestaDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CrearHabitacionDto request) {
        HabitacionRespuestaDto data = dispositivoService.updateRoom(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Ambiente actualizado exitosamente", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        dispositivoService.deleteRoom(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Ambiente eliminado exitosamente", null));
    }
}
