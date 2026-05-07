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
public class AlertaRespuestaDto {

    private Long id;
    private String tipo;
    private String mensaje;
    private boolean leido;

    @JsonProperty("fecha_creacion")
    private LocalDateTime fechaCreacion;

    @JsonProperty("device_id")
    private Long deviceId;

    @JsonProperty("device_name")
    private String deviceName;
}
