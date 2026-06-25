package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VerificacionEnviadaRespuestaDto {

    private String correo;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;
}
