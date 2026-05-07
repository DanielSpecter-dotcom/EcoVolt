package com.ecovolt.demo.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadoDeseadoDispositivoDto {

    @NotNull(message = "El dispositivo es obligatorio")
    @Positive(message = "El dispositivo debe ser valido")
    @JsonProperty("device_id")
    private Long deviceId;

    @NotNull(message = "El estado deseado es obligatorio")
    @JsonProperty("desired_on")
    private Boolean desiredOn;
}
