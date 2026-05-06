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
public class ModoAusenteResponseDto {

    @JsonProperty("home_id")
    private Long homeId;

    @JsonProperty("away_mode_enabled")
    private Boolean modoAusente;

    @JsonProperty("paused_routines")
    private Integer rutinasPausadas;
}
