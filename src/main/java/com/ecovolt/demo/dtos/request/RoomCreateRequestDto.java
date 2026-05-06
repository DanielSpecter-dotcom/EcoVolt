package com.ecovolt.demo.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomCreateRequestDto {

    @NotNull(message = "El usuario es obligatorio")
    @Positive(message = "El usuario debe ser valido")
    @JsonProperty("usuario_id")
    private Long usuarioId;

    @NotBlank(message = "El nombre del ambiente es obligatorio")
    @Size(max = 80, message = "El nombre del ambiente no debe superar 80 caracteres")
    private String nombre;
}
