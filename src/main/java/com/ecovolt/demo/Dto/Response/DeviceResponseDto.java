package com.ecovolt.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DeviceResponseDto {

    private Long id;
    private String nombre;
    private String tipo;

    @JsonProperty("potencia_watts")
    private Double potenciaWatts;

    @JsonProperty("habitacion_id")
    private Long habitacionId;
}
