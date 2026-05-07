package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
public class CrearRutinaDto {

    @NotNull(message = "La casa es obligatoria")
    @Positive(message = "La casa debe ser valida")
    @JsonProperty("home_id")
    private Long homeId;

    @NotBlank(message = "El nombre de la rutina es obligatorio")
    @Size(max = 100, message = "El nombre de la rutina no debe superar 100 caracteres")
    private String nombre;

    @NotNull(message = "La hora de ejecucion es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    @JsonProperty("execution_time")
    private LocalTime tiempoEjecucion;

    @NotEmpty(message = "Debe indicar al menos un dia de ejecucion")
    @JsonProperty("days_of_week")
    private Set<DayOfWeek> diasSemana;

    @Valid
    @NotEmpty(message = "La rutina debe incluir al menos una accion")
    private Set<AccionDispositivoRutinaDto> acciones;
}
