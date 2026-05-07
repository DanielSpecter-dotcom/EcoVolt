package com.ecovolt.demo.dtos.response;

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
public class EstadoDispositivoRespuestaDto {

    @JsonProperty("device_id")
    private Long deviceId;

    @JsonProperty("desired_on")
    private Boolean desiredOn;
}
