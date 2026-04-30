package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Request.DeviceCreateDto;
import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.DeviceResponseDto;
import com.ecovolt.demo.Service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceResponseDto>> create(@Valid @RequestBody DeviceCreateDto request) {
        DeviceResponseDto data = deviceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Dispositivo virtual registrado exitosamente", data));
    }
}
