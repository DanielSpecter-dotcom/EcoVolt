package com.ecovolt.demo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwayModeRequestDto {

    @NotNull(message = "Debe indicar si el modo ausente esta activo")
    @JsonProperty("away_mode_enabled")
    private Boolean awayModeEnabled;
}
