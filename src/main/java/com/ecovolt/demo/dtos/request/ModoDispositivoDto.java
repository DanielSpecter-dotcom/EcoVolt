package com.ecovolt.demo.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModoDispositivoDto {

    @NotBlank(message = "El modo es obligatorio")
    @Pattern(regexp = "AUTOMATIC|MANUAL", message = "El modo debe ser AUTOMATIC o MANUAL")
    private String mode;
}
