package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccionDispositivoRutinaDto {

    @NotNull(message = "El dispositivo es obligatorio")
    @Positive(message = "El dispositivo debe ser valido")
    @JsonProperty("device_id")
    private Long deviceId;

    @NotNull(message = "La accion de encendido/apagado es obligatoria")
    @JsonProperty("encendido")
    private Boolean encendido;
}
