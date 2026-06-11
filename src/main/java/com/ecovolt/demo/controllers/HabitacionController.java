package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.CrearHabitacionDto;
import com.ecovolt.demo.dtos.RespuestaApi;
import com.ecovolt.demo.dtos.HabitacionDTO;
import com.ecovolt.demo.serviceimpl.HabitacionService;
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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ecovolt.demo.security.CustomUserDetails;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@Tag(name = "Ambientes", description = "Gestion de ambientes del hogar")
public class HabitacionController {

    @Autowired
    private HabitacionService habitacionService;

    @Operation(summary = "Crear un nuevo ambiente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ambiente creado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @PostMapping("/insertarhabitacion")
    public ResponseEntity<RespuestaApi<HabitacionDTO>> create(@Valid @RequestBody CrearHabitacionDto request) {
        HabitacionDTO data = habitacionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Ambiente creado exitosamente", data));
    }

    @GetMapping("/listarhabitaciones")
    public ResponseEntity<RespuestaApi<List<HabitacionDTO>>> findAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<HabitacionDTO> data = habitacionService.findAll(userDetails.getId());
        return ResponseEntity.ok(new RespuestaApi<>(true, "Ambientes obtenidos exitosamente", data));
    }

    @GetMapping("/encontrarhabitacion/{id}")
    public ResponseEntity<RespuestaApi<HabitacionDTO>> findById(@PathVariable Long id) {
        HabitacionDTO data = habitacionService.findById(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Ambiente obtenido exitosamente", data));
    }

    @PutMapping("/actualizarhabitacion/{id}")
    public ResponseEntity<RespuestaApi<HabitacionDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody CrearHabitacionDto request) {
        HabitacionDTO data = habitacionService.update(id, request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Ambiente actualizado exitosamente", data));
    }

    @DeleteMapping("/eliminarhabitacion/{id}")
    public ResponseEntity<RespuestaApi<Void>> delete(@PathVariable Long id) {
        habitacionService.delete(id);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Ambiente eliminado exitosamente", null));
    }
}
