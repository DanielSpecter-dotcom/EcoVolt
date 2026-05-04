package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Request.DeviceCreateDto;
import com.ecovolt.demo.Dto.Request.DeviceModeRequestDto;
import com.ecovolt.demo.Dto.Request.DeviceRoomRequestDto;
import com.ecovolt.demo.Dto.Request.DeviceStatusRequestDto;
import com.ecovolt.demo.Dto.Request.DeviceUpdateRequestDto;
import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.DeviceResponseDto;
import com.ecovolt.demo.Service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Dispositivos", description = "Gestion de dispositivos del hogar")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "Registrar un dispositivo virtual")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Dispositivo creado")
    @PostMapping
    public ResponseEntity<ApiResponse<DeviceResponseDto>> create(@Valid @RequestBody DeviceCreateDto request) {
        DeviceResponseDto data = deviceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Dispositivo virtual registrado exitosamente", data));
    }

    @Operation(summary = "Listar dispositivos", description = "Lista todos los dispositivos no eliminados con su estado actual")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dispositivos obtenidos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceResponseDto>>> findAll() {
        List<DeviceResponseDto> data = deviceService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Dispositivos obtenidos exitosamente", data));
    }

    @Operation(summary = "Obtener detalle de un dispositivo")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dispositivo obtenido")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> findById(@PathVariable Long id) {
        DeviceResponseDto data = deviceService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dispositivo obtenido exitosamente", data));
    }

    @Operation(summary = "Asignar dispositivo a un ambiente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ambiente asignado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo o ambiente no encontrado")
    @PatchMapping("/{id}/room")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> assignRoom(
            @PathVariable Long id,
            @Valid @RequestBody DeviceRoomRequestDto request) {
        DeviceResponseDto data = deviceService.assignRoom(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ambiente asignado exitosamente", data));
    }

    @Operation(summary = "Actualizar configuracion de un dispositivo")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dispositivo actualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo o ambiente no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody DeviceUpdateRequestDto request) {
        DeviceResponseDto data = deviceService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dispositivo actualizado exitosamente", data));
    }

    @Operation(summary = "Eliminar dispositivo", description = "Realiza eliminacion logica para conservar los historicos en reportes")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dispositivo eliminado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo no encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dispositivo eliminado exitosamente", null));
    }

    @Operation(summary = "Cambiar estado de un dispositivo", description = "Permite alternar entre ON y OFF")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado actualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo no encontrado")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody DeviceStatusRequestDto request) {
        DeviceResponseDto data = deviceService.updateStatus(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Estado del dispositivo actualizado exitosamente", data));
    }

    @Operation(summary = "Cambiar modo de funcionamiento", description = "Permite alternar entre AUTOMATIC y MANUAL")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Modo actualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dispositivo no encontrado")
    @PatchMapping("/{id}/mode")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> updateMode(
            @PathVariable Long id,
            @Valid @RequestBody DeviceModeRequestDto request) {
        DeviceResponseDto data = deviceService.updateMode(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Modo del dispositivo actualizado exitosamente", data));
    }
}
