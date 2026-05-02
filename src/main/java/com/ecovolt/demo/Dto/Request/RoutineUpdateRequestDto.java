package com.ecovolt.demo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
public class RoutineUpdateRequestDto {

    @Size(max = 100, message = "El nombre de la rutina no debe superar 100 caracteres")
    private String name;

    @JsonFormat(pattern = "HH:mm")
    @JsonProperty("execution_time")
    private LocalTime executionTime;

    @JsonProperty("days_of_week")
    private Set<DayOfWeek> daysOfWeek;

    @Valid
    private Set<RoutineDeviceActionRequestDto> actions;

    @JsonProperty("enabled")
    private Boolean enabled;

    @Positive(message = "La casa debe ser valida")
    @JsonProperty("home_id")
    private Long homeId;
}
