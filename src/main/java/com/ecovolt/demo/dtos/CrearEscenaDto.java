package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrearEscenaDto {

    @NotBlank(message = "El nombre de la escena es obligatorio")
    @Size(max = 100, message = "El nombre de la escena no debe superar 100 caracteres")
    private String nombre;

    @Valid
    @NotEmpty(message = "La escena debe incluir al menos un dispositivo")
    @JsonProperty("devices")
    private List<EstadoDeseadoDispositivoDto> dispostivos;
}
