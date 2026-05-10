package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoDeseadoDispositivoDto {

    @NotNull(message = "El dispositivo es obligatorio")
    @Positive(message = "El dispositivo debe ser valido")
    @JsonProperty("device_id")
    private Long deviceId;

    @NotNull(message = "El estado deseado es obligatorio")
    @JsonProperty("desired_on")
    private Boolean desiredOn;
}
