package com.ecovolt.demo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceCreateDto {

    @NotNull(message = "El usuario es obligatorio")
    @Positive(message = "El usuario debe ser valido")
    @JsonProperty("usuario_id")
    private Long usuarioId;

    @NotBlank(message = "El tipo de dispositivo es obligatorio")
    @Size(max = 80, message = "El tipo de dispositivo no debe superar 80 caracteres")
    @JsonProperty("tipo_dispositivo")
    private String tipoDispositivo;

    @NotNull(message = "La potencia estimada es obligatoria")
    @Positive(message = "La potencia estimada debe ser mayor que cero")
    @JsonProperty("potencia_estimada_watts")
    private Double potenciaEstimadaWatts;
}
