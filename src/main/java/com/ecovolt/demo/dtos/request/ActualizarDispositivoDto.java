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
public class ActualizarDispositivoDto {

    @NotBlank(message = "El nombre del dispositivo es obligatorio")
    @Size(max = 80, message = "El nombre del dispositivo no debe superar 80 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo de dispositivo es obligatorio")
    @Size(max = 80, message = "El tipo de dispositivo no debe superar 80 caracteres")
    private String tipo;

    @NotNull(message = "La potencia es obligatoria")
    @Positive(message = "La potencia debe ser mayor que cero")
    @JsonProperty("power")
    private Double power;

    @NotNull(message = "El ambiente es obligatorio")
    @Positive(message = "El ambiente debe ser valido")
    @JsonProperty("room_id")
    private Long roomId;
}
