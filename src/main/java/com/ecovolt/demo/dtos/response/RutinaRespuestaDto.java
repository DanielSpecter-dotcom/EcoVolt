package com.ecovolt.demo.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutinaRespuestaDto {

    private Long id;

    @JsonProperty("home_id")
    private Long homeId;

    private String name;

    @JsonFormat(pattern = "HH:mm")
    @JsonProperty("execution_time")
    private LocalTime executionTime;

    @JsonProperty("days_of_week")
    private Set<DayOfWeek> daysOfWeek;

    private Set<AccionDispositivoRutinaRespuestaDto> actions;

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("paused_by_away_mode")
    private Boolean pausedByAwayMode;
}
