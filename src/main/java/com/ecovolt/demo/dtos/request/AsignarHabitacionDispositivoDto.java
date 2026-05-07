package com.ecovolt.demo.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignarHabitacionDispositivoDto {

    @NotNull(message = "El ambiente es obligatorio")
    @Positive(message = "El ambiente debe ser valido")
    @JsonProperty("room_id")
    private Long roomId;
}
