package com.ecovolt.demo.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationSettingsDto {

    @NotNull(message = "La configuracion de consumo excesivo es obligatoria")
    @JsonProperty("consumo_excesivo")
    private Boolean consumoExcesivo;

    @NotNull(message = "La configuracion de uso prolongado es obligatoria")
    @JsonProperty("uso_prolongado")
    private Boolean usoProlongado;

    @NotNull(message = "La configuracion de reporte semanal es obligatoria")
    @JsonProperty("reporte_semanal")
    private Boolean reporteSemanal;
}
