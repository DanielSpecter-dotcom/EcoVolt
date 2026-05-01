package com.ecovolt.demo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertLimitRequestDto {

    @NotNull(message = "El dispositivo es obligatorio")
    @Positive(message = "El dispositivo debe ser valido")
    @JsonProperty("device_id")
    private Long deviceId;

    @NotNull(message = "El limite de consumo es obligatorio")
    @Positive(message = "El limite de consumo debe ser mayor que cero")
    @JsonProperty("limit_kwh")
    private Double limitKwh;
}
