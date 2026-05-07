package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarDispositivoDto {

    @NotBlank(message = "El nombre del dispositivo es obligatorio")
    @Size(max = 80, message = "El nombre del dispositivo no debe superar 80 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo de dispositivo es obligatorio")
    @Size(max = 80, message = "El tipo de dispositivo no debe superar 80 caracteres")
    @JsonAlias("tipo_dispositivo")
    private String tipo;

    private Boolean activo;

    private Boolean automatico;

    @PositiveOrZero(message = "El limite debe ser mayor o igual que cero")
    @JsonProperty("limite_kwh")
    private Double limiteKwh;

    @Positive(message = "El ambiente debe ser valido")
    @JsonProperty("room_id")
    private Long roomId;
}
