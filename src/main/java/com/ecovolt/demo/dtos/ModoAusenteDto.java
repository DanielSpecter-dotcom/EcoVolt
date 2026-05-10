package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModoAusenteDto {

    @NotNull(message = "Debe indicar si el modo ausente esta activo")
    @JsonProperty("away_mode_enabled")
    private Boolean modoAusente;
}
