package com.ecovolt.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificarCorreoDto {

    @NotBlank(message = "El token es obligatorio")
    private String token;
}
