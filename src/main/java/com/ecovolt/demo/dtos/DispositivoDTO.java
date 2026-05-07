package com.ecovolt.demo.dtos;

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
public class DispositivoDTO {

    private Long id;
    private String nombre;
    private String tipo;

    //@JsonProperty("potencia_watts")
    //private Double potenciaWatts;

    private String status;
    private String mode;

    @JsonProperty("habitacion_id")
    private Long habitacionId;

    @JsonProperty("habitacion_nombre")
    private String habitacionNombre;
}
