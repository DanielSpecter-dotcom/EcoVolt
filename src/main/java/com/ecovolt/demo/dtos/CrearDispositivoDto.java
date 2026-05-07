package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearDispositivoDto {

    @NotNull(message = "El usuario es obligatorio")
    @Positive(message = "El usuario debe ser valido")
    @JsonProperty("usuario_id")
    private Long usuarioId;

    @NotBlank(message = "El nombre del dispositivo es obligatorio")
    @Size(max = 80, message = "El nombre del dispositivo no debe superar 80 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo de dispositivo es obligatorio")
    @Size(max = 80, message = "El tipo de dispositivo no debe superar 80 caracteres")
    @JsonAlias("tipo_dispositivo")
    private String tipo;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    @NotNull(message = "El modo automatico es obligatorio")
    private Boolean automatico;

    @PositiveOrZero(message = "El limite debe ser mayor o igual que cero")
    @JsonProperty("limite_kwh")
    private Double limiteKwh;
}
