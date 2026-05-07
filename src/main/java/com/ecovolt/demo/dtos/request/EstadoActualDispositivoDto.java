package com.ecovolt.demo.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadoActualDispositivoDto {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ON|OFF", message = "El estado debe ser ON u OFF")
    private String status;
}
