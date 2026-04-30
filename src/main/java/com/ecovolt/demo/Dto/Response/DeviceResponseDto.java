package com.ecovolt.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeviceResponseDto {

    private Long id;
    private String nombre;
    private String tipo;

    @JsonProperty("potencia_watts")
    private Double potenciaWatts;

    @JsonProperty("habitacion_id")
    private Long habitacionId;
}
