package com.ecovolt.demo.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoRespuestaDto {

    private Long id;

    @JsonProperty("fecha_registro")
    private LocalDateTime fechaRegistro;

    @JsonProperty("kwh_consumidos")
    private Double kwhConsumidos;

    @JsonProperty("duracion_minutos")
    private Integer duracionMinutos;

    @JsonProperty("dispositivo_id")
    private Long dispositivoId;

    @JsonProperty("dispositivo_nombre")
    private String dispositivoNombre;
}
