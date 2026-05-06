package com.ecovolt.demo.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordDto {

    @NotBlank(message = "La contrasena actual es obligatoria")
    @JsonProperty("contrasena_actual")
    private String contrasenaActual;

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La nueva contrasena debe tener entre 8 y 100 caracteres")
    @JsonProperty("nueva_contrasena")
    private String nuevaContrasena;
}
